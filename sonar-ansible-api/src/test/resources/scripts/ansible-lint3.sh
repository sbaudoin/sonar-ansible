#!/bin/sh

echo Bla bla
echo src/test/resources/playbooks/playbook1.yml:Bla bla
echo src/test/resources/playbooks/playbook1.yml:2: [EUNKNOWN] Bla bla
echo src/test/resources/playbooks/playbook1.yml:3: [EAnyCheck1] An error $1
echo src/test/resources/playbooks/playbook1.yml:4: [AnyCheck2] Another error
echo src/test/resources/playbooks/playbook1.yml:5: [EAnyCheck2] Another error $3
echo src/test/resources/playbooks/playbook2.yml:3: [EAnyCheck2] Another error $4
