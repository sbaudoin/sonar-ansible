#!/bin/bash

export SONARQUBE_VERSION="$1"
export SCANNER_VERSION="$2"
if [ -z "$SCANNER_VERSION" ]
then
    echo "Missing parameters: <SonarQube version> <scanner version>" >&2
    exit 1
fi

export SCRIPT_DIR=`dirname $0`

# Clean-up if needed
echo "Cleanup..."
docker-compose -f $SCRIPT_DIR/docker-compose.yml down

# Start containers
echo "Starting SonarQube..."
#CONTAINER_NAME=`docker-compose -f $SCRIPT_DIR/docker-compose.yml up -d sonarqube 2>&1 | grep -o '[^ ]*sonarqube[^ ]*' | head -1`
docker-compose -f $SCRIPT_DIR/docker-compose.yml up -d sonarqube
CONTAINER_NAME=it_sonarqube_1
# Wait for SonarQube to be up
grep -q "SonarQube is up" <(docker logs --follow --tail 0 $CONTAINER_NAME)
# Copy the plugins
sudo pip install lxml
YAML_PLUGIN_VERSION=$(python -c "
from lxml import etree

pom = etree.parse('pom.xml')
print pom.xpath('/a:project/a:dependencyManagement//a:dependency[a:artifactId=\'sonar-yaml-plugin\']/a:version', namespaces={'a': 'http://maven.apache.org/POM/4.0.0'})[0].text
")
wget -O /tmp/sonar-yaml-plugin-$YAML_PLUGIN_VERSION.jar https://oss.sonatype.org/content/groups/public/com/github/sbaudoin/sonar-yaml-plugin/$YAML_PLUGIN_VERSION/sonar-yaml-plugin-$YAML_PLUGIN_VERSION.jar
docker cp /tmp/sonar-yaml-plugin-$YAML_PLUGIN_VERSION.jar $CONTAINER_NAME:/opt/sonarqube/extensions/plugins
docker cp $SCRIPT_DIR/../sonar-ansible-plugin/target/sonar-ansible-plugin-*.jar $CONTAINER_NAME:/opt/sonarqube/extensions/plugins
# Restart SonarQube
docker-compose -f $SCRIPT_DIR/docker-compose.yml restart sonarqube
# Wait for SonarQube to be up
grep -q "SonarQube is up" <(docker logs --follow --tail 0 $CONTAINER_NAME)
# Check plug-in installation
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
