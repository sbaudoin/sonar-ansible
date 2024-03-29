#!/bin/bash -e

if [ -n "$AL_VERSION" ]
then
    LINTER_VERSION="==$AL_VERSION"
fi
echo "Testing with ansible-lint version: ${AL_VERSION:-latest}"
if [ -n "$ANSIBLE_VERSION" ]
then
    PIP_ANSIBLE="ansible==$ANSIBLE_VERSION"
fi
echo "Testing with ansible version: ${ANSIBLE_VERSION:-latest}"

# Install requirements
echo "Installing requirements..."
apt-get -qq update
apt-get -qq remove -y python > /dev/null
apt-get -qq autoremove -y > /dev/null
apt-get -qq install -y python3-pip > /dev/null
if [ "$(python3 --version)" == "Python 3.5.3" ]
then
    patch /usr/lib/python3.5/weakref.py < /usr/src/myapp/it/py35.patch
fi
pip3 install -q "rich<=10.16.2" ansible-lint$LINTER_VERSION $PIP_ANSIBLE requests requests[security]

# Create quality profile to enable Ansible rules
echo "Enabling Ansible rules..."
curl -s -o /dev/null -w "%{http_code}\n" -u admin:admin -X POST 'http://sonarqube:9000/api/qualityprofiles/create?name=Ansible&language=yaml' | grep -q 200
curl -s -o /dev/null -w "%{http_code}\n" -u admin:admin -X POST 'http://sonarqube:9000/api/qualityprofiles/set_default?qualityProfile=Ansible&language=yaml' | grep -q 204
PROFILE_KEY=`curl -s -u admin:admin 'http://sonarqube:9000/api/qualityprofiles/search?qualityProfile=Ansible&language=yaml' | sed 's/.*"key":"\([^"]*\)".*/\1/'`
curl -s -o /dev/null -w "%{http_code}\n" -u admin:admin -X POST 'http://sonarqube:9000/api/qualityprofiles/activate_rules?targetKey='$PROFILE_KEY'&tags=ansible' | grep -q 200

# Disable warnings
echo "Disable ansible-lint warnings..."
curl -s -o /dev/null -w "%{http_code}\n" -u admin:admin -X POST 'http://sonarqube:9000/api/settings/set?key=sonar.ansible.ansiblelint.disable_warnings&value=true' | grep -q 204

# Install sonar-runner
echo "Installing Sonar scanner..."
cd /tmp
wget -q https://binaries.sonarsource.com/Distribution/sonar-scanner-cli/sonar-scanner-cli-$SCANNER_VERSION.zip
unzip -q sonar-scanner-cli-$SCANNER_VERSION.zip
export PATH=/tmp/sonar-scanner-$SCANNER_VERSION/bin:$PATH

# Configure sonar-runner
echo "sonar.host.url=http://sonarqube:9000" > /tmp/sonar-scanner-$SCANNER_VERSION/conf/sonar-scanner.properties

# Audit code
echo "Launching scanner..."
cd /usr/src/myapp/it
sonar-scanner 2>&1 | tee /tmp/scanner.log
if [ $? -ne 0 ]
then
    echo "Error scanning Ansible playbooks" >&2
    exit 1
fi

# Check for warnings
if grep -q "^WARN: " /tmp/scanner.log
then
    echo "Warnings found" >&2
    exit 1
fi

# Sleep a little because SonarQube needs some time to ingest the audit results
sleep 10

# Check audit result
echo "Checking result..."
python3 << EOF
from __future__ import print_function
import requests
import sys

r = requests.get('http://sonarqube:9000/api/measures/component?component=my:project&metricKeys=ncloc,comment_lines,lines,files,directories,violations', auth=('admin', 'admin'))
if r.status_code != 200:
    sys.exit(1)

data = r.json()

if 'component' not in data or 'measures' not in data['component']:
    print('Invalid server response: wrong JSON', file=sys.stderr)
    sys.exit(1)

print('Measures: {}'.format(str(data['component']['measures'])))

lines = ncloc = files = directories = comment_lines = violations = False
for measure in data['component']['measures']:
    if measure['metric'] == 'lines' and measure['value'] == '134':
        print('lines metrics OK')
        lines = True
#    if measure['metric'] == 'ncloc' and measure['value'] == '87':
#        print('ncloc metrics OK')
#        ncloc = True
    ncloc = True
    if measure['metric'] == 'files' and measure['value'] == '13':
        print('files metrics OK')
        files = True
#    if measure['metric'] == 'comment_lines' and measure['value'] == '1':
#        print('comment_lines metrics OK')
#        comment_lines = True
    comment_lines = True
    if measure['metric'] == 'violations' and measure['value'] == '28':
        print('violations metrics OK')
        violations = True

r = requests.get('http://sonarqube:9000/api/issues/search?componentKeys=my:project:src/roles/bobbins/tasks/main.yml&statuses=OPEN', auth=('admin', 'admin'))
if r.status_code != 200:
    print('Invalid server response: ' + str(r.status_code), file=sys.stderr)
    sys.exit(1)

data = r.json()

if data['total'] != 1:
    print('Wrong total number of issues: ' + str(data['total']), file=sys.stderr)
    sys.exit(1)
issues = False
if data['issues'][0]['message'] == 'Git checkouts must contain explicit version' and data['issues'][0]['line'] == 2:
    print('issues metrics OK')
    issues = True

sys.exit(0 if lines and ncloc and files and comment_lines and violations and issues else 1)
EOF
