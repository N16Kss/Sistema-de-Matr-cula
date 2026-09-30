import java.io.IOException;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Scanner;

public class AplicacaoCLI {
    private final Scanner scanner;
    private final FileStorage<Usuario> usuarios =
            new FileStorage<>(Path.of("dados", "usuarios.bin"), Usuario.class);
    private final FileStorage<Curso> cursos =
            new FileStorage<>(Path.of("dados", "cursos.bin"), Curso.class);
    private final FileStorage<Disciplina> disciplinas =
            new FileStorage<>(Path.of("dados", "disciplinas.bin"), Disciplina.class);
    private final FileStorage<Turma> turmas =
            new FileStorage<>(Path.of("dados", "turmas.bin"), Turma.class);
    private final FileStorage<Matricula> matriculas =
            new FileStorage<>(Path.of("dados", "matriculas.bin"), Matricula.class);

    public AplicacaoCLI(Scanner scanner) {
        this.scanner = scanner;
    }

    public static void main(String[] args) {
        try (Scanner scanner = new Scanner(System.in)) {
            new AplicacaoCLI(scanner).executar();
        }
    }

    public void executar() {
        boolean executando = true;
        while (executando) {
            System.out.println("\n=== Sistema de Matrícula ===");
            System.out.println("1. Entrar");
            System.out.println("2. Criar conta");
            System.out.println("0. Sair");
            int opcao = lerOpcao("Opção: ", 0, 2);
            switch (opcao) {
                case 1 -> entrar();
                case 2 -> cadastrar();
                case 0 -> executando = false;
                default -> executando = false;
            }
        }
        System.out.println("Sistema encerrado.");
    }

    private void entrar() {
        String matricula = lerObrigatorio("Matrícula: ");
        if (matricula == null) {
            return;
        }
        String senha = lerObrigatorio("Senha: ");
        if (senha == null) {
            return;
        }

        try {
            Usuario usuario = usuarios.readAll().stream()
                    .filter(item -> item.getMatricula().equals(matricula))
                    .findFirst().orElse(null);
            if (usuario == null || !usuario.autenticar(senha)) {
                System.out.println("Matrícula ou senha inválida.");
                return;
            }
            System.out.println("Acesso autorizado.");
            if (usuario instanceof Aluno aluno) {
                menuAluno(aluno);
            } else if (usuario instanceof Professor professor) {
                menuProfessor(professor);
            } else if (usuario instanceof Secretaria secretaria) {
                menuSecretaria(secretaria);
            } else {
                System.out.println("Perfil de usuário não reconhecido.");
            }
        } catch (IOException exception) {
            mostrarErro(exception);
        }
    }

    private void cadastrar() {
        System.out.println("\nCriar conta");
        System.out.println("1. Aluno");
        System.out.println("2. Professor");
        System.out.println("3. Secretaria");
        System.out.println("0. Voltar");
        int perfil = lerOpcao("Perfil: ", 0, 3);
        if (perfil == 0 || perfil < 0) {
            return;
        }

        String matricula = lerObrigatorio("Matrícula: ");
        if (matricula == null) {
            return;
        }
        String senha = lerObrigatorio("Senha: ");
        if (senha == null) {
            return;
        }

        try {
            Usuario usuario;
            if (perfil == 1) {
                String nome = lerObrigatorio("Nome: ");
                if (nome == null) {
                    return;
                }
                usuario = new Aluno(matricula, senha, nome, StatusAluno.ATIVO);
            } else if (perfil == 2) {
                String nome = lerObrigatorio("Nome: ");
                if (nome == null) {
                    return;
                }
                usuario = new Professor(matricula, senha, nome);
            } else {
                usuario = new Secretaria(matricula, senha);
                System.out.println("Aviso: contas de secretaria têm acesso administrativo.");
            }

            if (usuario.cadastrar()) {
                System.out.println("Conta criada. Você já pode entrar.");
            } else {
                System.out.println("Já existe uma conta com essa matrícula.");
            }
        } catch (IOException | IllegalArgumentException exception) {
            mostrarErro(exception);
        }
    }

    private void menuAluno(Aluno aluno) {
        boolean conectado = true;
        while (conectado) {
            System.out.println("\n=== Aluno: " + aluno.getNome() + " ===");
            System.out.println("1. Ver turmas abertas");
            System.out.println("2. Matricular-se");
            System.out.println("3. Cancelar matrícula");
            System.out.println("4. Consultar histórico");
            System.out.println("5. Atualizar nome");
            System.out.println("6. Alterar senha");
            System.out.println("0. Sair da conta");
            int opcao = lerOpcao("Opção: ", 0, 6);
            switch (opcao) {
                case 1 -> executarAcao(() -> listarTurmasAbertas(lerObrigatorio("Semestre: ")));
                case 2 -> executarAcao(() -> matricularAluno(aluno));
                case 3 -> executarAcao(() -> cancelarMatricula(aluno));
                case 4 -> executarAcao(() -> imprimirLista(aluno.consultarHistorico()));
                case 5 -> executarAcao(() -> atualizarAluno(aluno));
                case 6 -> executarAcao(() -> alterarSenha(aluno));
                case 0 -> conectado = false;
                default -> conectado = false;
            }
        }
    }

    private void menuProfessor(Professor professor) {
        boolean conectado = true;
        while (conectado) {
            System.out.println("\n=== Professor: " + professor.getNome() + " ===");
            System.out.println("1. Ver minhas turmas");
            System.out.println("2. Consultar alunos de uma turma");
            System.out.println("3. Atualizar nome");
            System.out.println("4. Alterar senha");
            System.out.println("0. Sair da conta");
            int opcao = lerOpcao("Opção: ", 0, 4);
            switch (opcao) {
                case 1 -> executarAcao(() -> listarTurmasDoProfessor(professor));
                case 2 -> executarAcao(() -> consultarTurmaDoProfessor(professor));
                case 3 -> executarAcao(() -> atualizarProfessor(professor));
                case 4 -> executarAcao(() -> alterarSenha(professor));
                case 0 -> conectado = false;
                default -> conectado = false;
            }
        }
    }

    private void menuSecretaria(Secretaria secretaria) {
        boolean conectado = true;
        while (conectado) {
            System.out.println("\n=== Secretaria ===");
            System.out.println("1. Cadastrar curso");
            System.out.println("2. Adicionar disciplina a curso");
            System.out.println("3. Abrir turma");
            System.out.println("4. Configurar período de matrícula");
            System.out.println("5. Gerar currículo do semestre");
            System.out.println("6. Cancelar turmas abaixo do mínimo");
            System.out.println("7. Concluir turma e registrar histórico");
            System.out.println("8. Processar fila de cobrança (simulação)");
            System.out.println("9. Alterar senha");
            System.out.println("0. Sair da conta");
            int opcao = lerOpcao("Opção: ", 0, 9);
            switch (opcao) {
                case 1 -> executarAcao(this::cadastrarCurso);
                case 2 -> executarAcao(this::adicionarDisciplina);
                case 3 -> executarAcao(this::abrirTurma);
                case 4 -> executarAcao(this::configurarPeriodo);
                case 5 -> executarAcao(() -> gerarCurriculo(secretaria));
                case 6 -> executarAcao(this::cancelarTurmasAbaixoDoMinimo);
                case 7 -> executarAcao(this::concluirTurma);
                case 8 -> executarAcao(this::processarCobrancas);
                case 9 -> executarAcao(() -> alterarSenha(secretaria));
                case 0 -> conectado = false;
                default -> conectado = false;
            }
        }
    }

    private void listarTurmasAbertas(String semestre) throws IOException {
        if (semestre == null) {
            return;
        }
        List<Turma> abertas = turmas.readAll().stream()
                .filter(turma -> turma.getSemestre().equals(semestre)
                        && turma.getStatus() == StatusTurma.ABERTA)
                .toList();
        imprimirTurmas(abertas);
    }

    private void matricularAluno(Aluno aluno) throws IOException {
        String semestre = lerObrigatorio("Semestre: ");
        if (semestre == null) {
            return;
        }
        listarTurmasAbertas(semestre);
        String codigo = lerObrigatorio("Código da turma: ");
        if (codigo == null) {
            return;
        }
        Turma turma = turmas.readAll().stream()
                .filter(item -> item.getCodigo().equals(codigo)
                        && item.getSemestre().equals(semestre)
                        && item.getStatus() == StatusTurma.ABERTA)
                .findFirst().orElse(null);
        if (turma == null) {
            System.out.println("Turma aberta não encontrada nesse semestre.");
            return;
        }
        System.out.println(aluno.matricular(turma, semestre)
                ? "Matrícula confirmada. Cobrança adicionada à fila local."
                : "Matrícula não realizada. Verifique status, período e limites.");
    }

    private void cancelarMatricula(Aluno aluno) throws IOException {
        List<Matricula> ativas = matriculas.readAll().stream()
                .filter(matricula -> matricula.getAlunoMatricula().equals(aluno.getMatricula())
                        && matricula.getStatus() == StatusMatricula.CONFIRMADA)
                .toList();
        if (ativas.isEmpty()) {
            System.out.println("Você não possui matrículas confirmadas.");
            return;
        }
        for (int index = 0; index < ativas.size(); index++) {
            Matricula matricula = ativas.get(index);
            System.out.printf("%d. Turma %s, semestre %s%n", index + 1,
                    matricula.getTurmaCodigo(), matricula.getSemestre());
        }
        int escolha = lerOpcao("Matrícula (0 para voltar): ", 0, ativas.size());
        if (escolha <= 0) {
            return;
        }
        Matricula selecionada = ativas.get(escolha - 1);
        System.out.println(aluno.cancelarMatricula(selecionada.getTurmaCodigo(), selecionada.getSemestre())
                ? "Matrícula cancelada."
                : "Cancelamento não permitido fora do período de matrícula.");
    }

    private void atualizarAluno(Aluno aluno) throws IOException {
        String nome = lerObrigatorio("Novo nome: ");
        if (nome != null) {
            System.out.println(aluno.atualizarDados(nome, aluno.getStatus())
                    ? "Dados atualizados."
                    : "Nome inválido.");
        }
    }

    private void listarTurmasDoProfessor(Professor professor) throws IOException {
        List<Turma> minhas = turmas.readAll().stream()
                .filter(turma -> turma.getProfessorMatricula().equals(professor.getMatricula()))
                .toList();
        imprimirTurmas(minhas);
    }

    private void consultarTurmaDoProfessor(Professor professor) throws IOException {
        listarTurmasDoProfessor(professor);
        String codigo = lerObrigatorio("Código da turma: ");
        if (codigo == null) {
            return;
        }
        imprimirLista(professor.consultarAlunos(codigo));
        System.out.println("Total: " + professor.consultarQuantidadeAlunos(codigo));
    }

    private void atualizarProfessor(Professor professor) throws IOException {
        String nome = lerObrigatorio("Novo nome: ");
        if (nome != null) {
            System.out.println(professor.atualizarDados(nome)
                    ? "Dados atualizados."
                    : "Nome inválido.");
        }
    }

    private void cadastrarCurso() throws IOException {
        String nome = lerObrigatorio("Nome do curso: ");
        String creditos = lerObrigatorio("Créditos: ");
        if (nome == null || creditos == null) {
            return;
        }
        if (cursos.readAll().stream().anyMatch(curso -> curso.getNome().equals(nome))) {
            System.out.println("Já existe um curso com esse nome.");
            return;
        }
        new Curso(nome, creditos).salvar();
        System.out.println("Curso cadastrado.");
    }

    private void adicionarDisciplina() throws IOException {
        List<Curso> listaCursos = cursos.readAll();
        if (listaCursos.isEmpty()) {
            System.out.println("Cadastre um curso antes de adicionar disciplinas.");
            return;
        }
        for (int index = 0; index < listaCursos.size(); index++) {
            System.out.printf("%d. %s%n", index + 1, listaCursos.get(index).getNome());
        }
        int escolha = lerOpcao("Curso (0 para voltar): ", 0, listaCursos.size());
        if (escolha <= 0) {
            return;
        }
        Curso curso = listaCursos.get(escolha - 1);
        String codigo = lerObrigatorio("Código da disciplina: ");
        String nome = lerObrigatorio("Nome: ");
        if (codigo == null || nome == null) {
            return;
        }
        System.out.println("1. Obrigatória");
        System.out.println("2. Optativa");
        int tipoEscolhido = lerOpcao("Tipo: ", 1, 2);
        TipoDisciplina tipo = tipoEscolhido == 1 ? TipoDisciplina.OBRIGATORIA : TipoDisciplina.OPTATIVA;
        Disciplina disciplina = new Disciplina(codigo, nome, tipo);
        System.out.println(curso.adicionarDisciplina(disciplina)
                ? "Disciplina associada ao curso."
                : "Não foi possível associar a disciplina.");
    }

    private void abrirTurma() throws IOException {
        List<Disciplina> listaDisciplinas = disciplinas.readAll();
        if (listaDisciplinas.isEmpty()) {
            System.out.println("Cadastre disciplinas antes de abrir turmas.");
            return;
        }
        for (Disciplina disciplina : listaDisciplinas) {
            System.out.printf("%s. %s (%s)%n", disciplina.getCodigo(), disciplina.getNome(), disciplina.getTipo());
        }
        String codigoDisciplina = lerObrigatorio("Código da disciplina: ");
        String codigoTurma = lerObrigatorio("Código da turma: ");
        String matriculaProfessor = lerObrigatorio("Matrícula do professor: ");
        String semestre = lerObrigatorio("Semestre: ");
        if (codigoDisciplina == null || codigoTurma == null || matriculaProfessor == null || semestre == null) {
            return;
        }
        boolean disciplinaExiste = listaDisciplinas.stream()
                .anyMatch(disciplina -> disciplina.getCodigo().equals(codigoDisciplina));
        boolean professorExiste = usuarios.readAll().stream()
                .anyMatch(usuario -> usuario.getMatricula().equals(matriculaProfessor)
                        && usuario instanceof Professor);
        boolean codigoEmUso = turmas.readAll().stream()
                .anyMatch(turma -> turma.getCodigo().equals(codigoTurma));
        if (!disciplinaExiste || !professorExiste || codigoEmUso) {
            System.out.println("Disciplina ou professor inválido, ou código de turma já utilizado.");
            return;
        }
        new Turma(codigoTurma, codigoDisciplina, matriculaProfessor, semestre).salvar();
        System.out.println("Turma aberta.");
    }

    private void configurarPeriodo() throws IOException {
        String semestre = lerObrigatorio("Semestre: ");
        String inicio = lerObrigatorio("Data inicial (AAAA-MM-DD): ");
        String fim = lerObrigatorio("Data final (AAAA-MM-DD): ");
        if (semestre == null || inicio == null || fim == null) {
            return;
        }
        try {
            new JanelaMatricula(semestre, LocalDate.parse(inicio), LocalDate.parse(fim)).salvar();
            System.out.println("Período de matrícula salvo.");
        } catch (DateTimeParseException exception) {
            System.out.println("Data inválida. Use o formato AAAA-MM-DD.");
        }
    }

    private void gerarCurriculo(Secretaria secretaria) throws IOException {
        String semestre = lerObrigatorio("Semestre: ");
        if (semestre == null) {
            return;
        }
        Curriculo curriculo = secretaria.gerarCurriculo(semestre);
        System.out.println("\nAlunos:");
        imprimirArray(curriculo.consultarAlunos());
        System.out.println("Disciplinas e turmas:");
        imprimirArray(curriculo.consultarDisciplinas());
        System.out.println("Professores:");
        imprimirArray(curriculo.consultarProfessores());
    }

    private void cancelarTurmasAbaixoDoMinimo() throws IOException {
        int canceladas = 0;
        for (Turma turma : turmas.readAll()) {
            if (turma.getStatus() != StatusTurma.CANCELADA
                    && !JanelaMatricula.estaAberto(turma.getSemestre())
                    && turma.cancelarTurma()) {
                System.out.println("Turma cancelada: " + turma.getCodigo());
                canceladas++;
            }
        }
        System.out.println("Total de turmas canceladas: " + canceladas);
    }

    private void concluirTurma() throws IOException {
        imprimirTurmas(turmas.readAll().stream()
                .filter(turma -> turma.getStatus() != StatusTurma.CANCELADA)
                .toList());
        String codigo = lerObrigatorio("Código da turma: ");
        if (codigo == null) {
            return;
        }
        Turma turma = turmas.readAll().stream()
                .filter(item -> item.getCodigo().equals(codigo))
                .findFirst().orElse(null);
        if (turma == null) {
            System.out.println("Turma não encontrada.");
            return;
        }
        System.out.println(turma.concluirTurma()
                ? "Turma concluída e histórico atualizado."
                : "Não foi possível concluir a turma.");
    }

    private void processarCobrancas() throws IOException {
        List<NotificacaoCobranca> pendentes = NotificacaoCobranca.pendentes();
        if (pendentes.isEmpty()) {
            System.out.println("Não há cobranças pendentes.");
            return;
        }
        for (int index = 0; index < pendentes.size(); index++) {
            NotificacaoCobranca notificacao = pendentes.get(index);
            System.out.printf("%d. Aluno %s, turma %s, semestre %s%n", index + 1,
                    notificacao.getAlunoMatricula(), notificacao.getTurmaCodigo(), notificacao.getSemestre());
        }
        System.out.println("A marcação é uma simulação local, não envia dados a outro sistema.");
        int escolha = lerOpcao("Evento para marcar como enviado (0 para voltar): ", 0, pendentes.size());
        if (escolha > 0) {
            System.out.println(pendentes.get(escolha - 1).marcarEnviada()
                    ? "Evento marcado como enviado na simulação."
                    : "O evento não pôde ser atualizado.");
        }
    }

    private void alterarSenha(Usuario usuario) throws IOException {
        String atual = lerObrigatorio("Senha atual: ");
        String nova = lerObrigatorio("Nova senha: ");
        String confirmacao = lerObrigatorio("Confirme a nova senha: ");
        if (atual == null || nova == null || confirmacao == null) {
            return;
        }
        if (!nova.equals(confirmacao)) {
            System.out.println("As senhas não coincidem.");
            return;
        }
        System.out.println(usuario.mudarSenha(atual, nova)
                ? "Senha alterada."
                : "Senha atual incorreta ou nova senha inválida.");
    }

    private void imprimirTurmas(List<Turma> lista) throws IOException {
        if (lista.isEmpty()) {
            System.out.println("Nenhuma turma encontrada.");
            return;
        }
        List<Disciplina> listaDisciplinas = disciplinas.readAll();
        for (Turma turma : lista) {
            String nomeDisciplina = listaDisciplinas.stream()
                    .filter(disciplina -> disciplina.getCodigo().equals(turma.getDisciplinaCodigo()))
                    .map(Disciplina::getNome)
                    .findFirst().orElse("disciplina desconhecida");
            System.out.printf("%s | %s | turma %s | professor %s | semestre %s | %s alunos | %s%n",
                    turma.getDisciplinaCodigo(), nomeDisciplina, turma.getCodigo(),
                    turma.getProfessorMatricula(), turma.getSemestre(), Matricula.contarAtivas(turma.getCodigo()),
                    turma.getStatus());
        }
    }

    private void imprimirLista(List<String> valores) {
        if (valores.isEmpty()) {
            System.out.println("Nenhum registro encontrado.");
            return;
        }
        valores.forEach(System.out::println);
    }

    private void imprimirArray(String[] valores) {
        if (valores.length == 0) {
            System.out.println("Nenhum registro encontrado.");
            return;
        }
        for (String valor : valores) {
            System.out.println(valor);
        }
    }

    private String lerObrigatorio(String prompt) {
        while (true) {
            System.out.print(prompt);
            if (!scanner.hasNextLine()) {
                return null;
            }
            String valor = scanner.nextLine().trim();
            if (!valor.isEmpty()) {
                return valor;
            }
            System.out.println("O campo não pode ficar vazio.");
        }
    }

    private int lerOpcao(String prompt, int minimo, int maximo) {
        while (true) {
            System.out.print(prompt);
            if (!scanner.hasNextLine()) {
                return -1;
            }
            String entrada = scanner.nextLine().trim();
            try {
                int opcao = Integer.parseInt(entrada);
                if (opcao >= minimo && opcao <= maximo) {
                    return opcao;
                }
            } catch (NumberFormatException ignored) {
            }
            System.out.printf("Escolha um número entre %d e %d.%n", minimo, maximo);
        }
    }

    private void executarAcao(Acao acao) {
        try {
            acao.executar();
        } catch (IOException | IllegalArgumentException exception) {
            mostrarErro(exception);
        }
    }

    private void mostrarErro(Exception exception) {
        System.out.println("Não foi possível concluir a operação: " + exception.getMessage());
    }

    @FunctionalInterface
    private interface Acao {
        void executar() throws IOException;
    }
}