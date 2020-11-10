#!/bin/bash

export SONARQUBE_VERSION="$1"
export SCANNER_VERSION="$2"
export JAVA_VERSION="$3"
export AL_VERSION="$4"
export ANSIBLE_VERSION="$5"
if [ -z "$SCANNER_VERSION" ]
then
    echo "Missing parameters: <SonarQube version> <scanner version> [Java version] [ansible-lint version] [ansible version]" >&2
    exit 1
fi

export SCRIPT_DIR=`dirname $0`

# Clean-up if needed
echo "Cleanup..."
docker-compose -f $SCRIPT_DIR/docker-compose.yml down

# Start containers
echo "Starting SonarQube..."
docker-compose -f $SCRIPT_DIR/docker-compose.yml up -d sonarqube
CONTAINER_NAME=$(docker ps --format "{{.Names}}" | grep 'it_sonarqube_1.*' | head -1)
# Wait for SonarQube to be up
grep -q "SonarQube is up" <(docker logs --follow --tail 0 $CONTAINER_NAME)
# Copy the plugins
echo "Installing the plugins..."
sudo pip install -q lxml
YAML_PLUGIN_VERSION=$(python -c "
from lxml import etree

pom = etree.parse('pom.xml')
print pom.xpath('/a:project/a:dependencyManagement//a:dependency[a:artifactId=\'sonar-yaml-plugin\']/a:version', namespaces={'a': 'http://maven.apache.org/POM/4.0.0'})[0].text
")
wget -q -O /tmp/sonar-yaml-plugin-$YAML_PLUGIN_VERSION.jar https://oss.sonatype.org/content/groups/public/com/github/sbaudoin/sonar-yaml-plugin/$YAML_PLUGIN_VERSION/sonar-yaml-plugin-$YAML_PLUGIN_VERSION.jar
docker cp /tmp/sonar-yaml-plugin-$YAML_PLUGIN_VERSION.jar $CONTAINER_NAME:/opt/sonarqube/extensions/plugins
MAVEN_VERSION=$(python -c "
from lxml import etree

pom = etree.parse('pom.xml')
print pom.xpath('/a:project/a:version', namespaces={'a': 'http://maven.apache.org/POM/4.0.0'})[0].text
")
docker cp $SCRIPT_DIR/../sonar-ansible-plugin/target/sonar-ansible-plugin-$MAVEN_VERSION.jar $CONTAINER_NAME:/opt/sonarqube/extensions/plugins
# Restart SonarQube
docker-compose -f $SCRIPT_DIR/docker-compose.yml restart sonarqube
# Wait for SonarQube to be up
grep -q "SonarQube is up" <(docker logs --follow --tail 0 $CONTAINER_NAME)
# Check plug-in installation
docker exec -u root $CONTAINER_NAME bash -c "if grep -q Alpine /etc/issue; then apk update && apk add -q curl; fi"
if ! docker exec $CONTAINER_NAME curl -su admin:admin http://localhost:9000/api/plugins/installed | python -c '
import sys
import json

data = json.loads(sys.stdin.read())
if "plugins" in data:
    for plugin in data["plugins"]:
        if plugin["key"] == "ansible":
            sys.exit(0)
sys.exit(1)
'
then
    echo "Plugin not installed" >&2
    exit 1
fi

# Audit code
echo "Audit Ansible test playbooks..."
docker-compose -f $SCRIPT_DIR/docker-compose.yml up --build --exit-code-from auditor auditor
AUDIT_STATUS=$?

# Delete containers
echo "Cleanup..."
docker-compose -f $SCRIPT_DIR/docker-compose.yml down

exit $AUDIT_STATUS
