#!/bin/sh

echo Bla bla
echo playbooks/playbook1.yml:Bla bla
echo playbooks/playbook1.yml:2: [EUNKNOWN] Bla bla
echo playbooks/playbook1.yml:2: [EANSIBLE1] A first error
echo playbooks/playbook1.yml:3: [EAnyCheck1] An error $1
echo playbooks/playbook1.yml:4: [AnyCheck2] Another error
echo $PWD/playbooks/playbook1.yml:5: [EAnyCheck2] Another error $3
echo playbooks/playbook2.yml:3: [EAnyCheck2] Another error $4
