# Sistema-de-Matr-cula
Sistema de matrícula para uma universidade que deseja informatizar o sistema atual


### REQUISITOS DO SISTEMA
## FUNCIONAIS


| Requisito | Descrição |
|  --- | --- | 
| RF01 Gerar Currículo| Secretaria deve gerar o curriculo do semestre que contem informações sobre diciplinas, professores e alunos|
| RF02 Cadastro de Usuário| Sistema deve permitir com que professores, alunos se cadastrem do sistema, assim como atuaizarem seus dados|
| RF03 Gerenciar Matrícula| Aluno pode se matricular e gerenciar o estado da sua matrícula, podendo cancela-la|
| RF04 Autenticar| Sistema permite com que os usuários entrem no sistema com su autêncação de dados|
| RF05 Encerramento de Disciplina| Sistema deve encerrar incrição quando limite de alunos for atingido|
| RF06 Cancelamento de Disicplina| Sistema deve cancelar diciplinas sem  mínimo de alunos inscritos|
| RF07 Verificar Matriculados| Professor deve verficicar a quantidada e os alunos matriculados em cada diciplina| 
|RF08 Notificar Cobrança | Sistema de matrículas deve notifcar o sistema de cobranças para que alunos matriculados em diciplinas possam ser cobrados |





## REGRAS DE NEGÓCIO
|Regras de Negócio|Descrição|
| --- | --- |
|RN01 Limite de opções do aluno | Sistema deve limitar a disciplinas que o aluno pode se matrícular assim como suas especificações, 1°Obrigatorias. 2 outras Optativas |
|RN02 Capacidade máxima da turma | Tamanho máximo da turma deve ser 60|
|RN03 Numero mínimo para manter diciplina ativa | Número mínimo de alunos inscritos para manter uma disciplina ativa deve ser 3 |
|RN04 Restrição de Efeturar e cancelar Matrícula|Questões de matrícula só podem ser realizadas durante o período da matrícula | 

 

## DIAGRAMA DE CASOS DE USO




## ESPECIFICAÇÕES
Status do Aluno Existe





## V2


* NOVO REQUISITO
|Requisito|Descrição|
|RFO9 Histórico de Disciplinas| Sistema deve manter histórico de disciplinas cursadas|




* NOVO REGRA DE NEGÓCIO
|RN05 Status Aluno| Apenas alunos **Ativos** serão contabilizados, nos históricos, e nas Matrículas|

