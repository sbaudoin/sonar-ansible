@echo Bla bla
@echo playbooks/playbook1.yml:Bla bla
@echo Bla playbooks/playbook1.yml
@echo Bla playbooks/playbook1.yml:Bla
@echo playbooks/playbook1.yml:2: [EUNKNOWN] Bla bla
@echo UNKNOWN playbooks/playbook1.yml:2
@echo playbooks/playbook1.yml:2: [EANSIBLE1] A first error
@echo ANSIBLE1 playbooks/playbook1.yml:2
@echo playbooks/playbook1.yml:3: [EAnyCheck1] An error %1
@echo AnyCheck1 playbooks/playbook1.yml:4
@echo playbooks/playbook1.yml:4: [AnyCheck2] Another error
@echo %cd%/playbooks/playbook1.yml:5: [EAnyCheck2] Another error %4
@echo AnyCheck2 %cd%/playbooks/playbook1.yml:5
@echo playbooks/playbook2.yml:3: [EAnyCheck2] Another error %5
@echo AnyCheck2 playbooks/playbook2.yml:3
