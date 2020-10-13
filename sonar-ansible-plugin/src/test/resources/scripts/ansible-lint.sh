#!/bin/sh

echo playbooks/playbook1.yml:1: [EANSIBLE1] A first error
echo playbooks/playbook1.yml:3: [EAnyCheck1] An error $1
echo playbooks/playbook1.yml:5: [EAnyCheck2] Another error $2
echo playbooks/playbook2.yml:2: [EAnyCheck2] An error
