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


* NOVA REGRA DE NEGÓCIO
|RN05 Status Aluno| Apenas alunos **Ativos** serão contabilizados, nos históricos, e nas Matrículas|


## V3 

* NOVO REQUISITOO

|RF10 Manter histórico de disciplinas e turmas| Um aluno deve manter seu histórico de disciplinas cursadas e turmas p/cada disciplina|

* ALTERAÇÃO DE REQUISITO

| RF05 Encerramento de **turma**| Sistema deve encerrar incrição quando limite de alunos de turma for atingido|
| RF06 Cancelamento de **turma** | Sistema deve cancelar turmas sem  mínimo de alunos inscritos|

* ALTERAÇÃO DE REGRA DE NEGÓCIO

|RN03 Numero mínimo para manter **turma** ativa | Número mínimo de alunos inscritos para manter uma **turma** ativa deve ser 3 |




## MODELAGENS

- Professores e alunos são associados a turmas e não diretamente a disciplinas 
- Uma turma deve estar associada a uma disciplina
- Uma disciplina pode ter várias turmas, sendo que essas turmas devem ser idependentes entre si

## Implementação inicial

As classes Java persistem os dados em arquivos binários na pasta `dados/`, relativa ao diretório de execução. O armazenamento substitui cada arquivo por meio de gravação temporária, reduzindo o risco de arquivos parcialmente gravados.

| Arquivo | Dados |
| --- | --- |
| `usuarios.bin` | Alunos, professores e secretarias; senhas armazenadas com hash PBKDF2 e salt |
| `cursos.bin`, `disciplinas.bin`, `turmas.bin` | Catálogo e turmas do semestre |
| `matriculas.bin`, `periodos-matricula.bin` | Matrículas, histórico e períodos de inscrição |
| `curriculos.bin` | Currículos semestrais gerados |
| `fila-cobranca.bin` | Eventos pendentes para integração com o sistema de cobrança |

Para RN01, a implementação inicial interpreta a regra como limite de uma disciplina obrigatória e duas optativas por aluno/semestre. A fila de cobrança persiste os eventos, mas o envio ao sistema externo ainda depende da definição da integração e de suas credenciais/protocolo.

## Interface de linha de comando

Compile e execute a partir da raiz do projeto:

```powershell
javac -d out classes\*.java
java -cp out AplicacaoCLI
```

A CLI oferece menus para Aluno, Professor e Secretaria. O cadastro de Secretaria é aberto e concede acesso administrativo. A opção de cobrança apenas marca eventos como enviados na simulação local; ela não se comunica com um sistema externo. Os arquivos de dados são criados na pasta `dados/` relativa ao diretório de execução.


