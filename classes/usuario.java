import java.io.IOException;
import java.io.Serializable;
import java.nio.file.Path;
import java.security.GeneralSecurityException;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.List;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

public class Usuario implements Serializable {
    private static final long serialVersionUID = 1L;
    private static final int HASH_ITERATIONS = 120_000;
    private static final int HASH_LENGTH_BITS = 256;
    private static final Path FILE = Path.of("dados", "usuarios.bin");

    private String matricula;
    private String senhaHash;
    private String salt;

    public Usuario(String matricula, String senha) {
        if (matricula == null || matricula.isBlank() || senha == null || senha.isBlank()) {
            throw new IllegalArgumentException("Matrícula e senha são obrigatórias.");
        }
        this.matricula = matricula;
        definirSenha(senha);
    }

    public boolean cadastrar() throws IOException {
        List<Usuario> usuarios = storage().readAll();
        if (usuarios.stream().anyMatch(usuario -> usuario.matricula.equals(matricula))) {
            return false;
        }
        usuarios.add(this);
        storage().writeAll(usuarios);
        return true;
    }

    public boolean autenticar(String senha) throws IOException {
        return storage().readAll().stream()
                .filter(usuario -> usuario.matricula.equals(matricula))
                .anyMatch(usuario -> usuario.verificarSenha(senha));
    }

    public boolean mudarSenha(String senhaAtual, String novaSenha) throws IOException {
        if (novaSenha == null || novaSenha.isBlank()) {
            return false;
        }

        List<Usuario> usuarios = storage().readAll();
        for (Usuario usuario : usuarios) {
            if (usuario.matricula.equals(matricula) && usuario.verificarSenha(senhaAtual)) {
                usuario.definirSenha(novaSenha);
                definirSenha(novaSenha);
                storage().writeAll(usuarios);
                return true;
            }
        }
        return false;
    }

    public String getMatricula() {
        return matricula;
    }

    protected void salvarUsuario() throws IOException {
        List<Usuario> usuarios = storage().readAll();
        for (int index = 0; index < usuarios.size(); index++) {
            if (usuarios.get(index).matricula.equals(matricula)) {
                usuarios.set(index, this);
                storage().writeAll(usuarios);
                return;
            }
        }
        throw new IOException("Usuário não cadastrado: " + matricula);
    }

    protected void definirSenha(String senha) {
        byte[] saltBytes = new byte[16];
        new SecureRandom().nextBytes(saltBytes);
        salt = Base64.getEncoder().encodeToString(saltBytes);
        senhaHash = gerarHash(senha, saltBytes);
    }

    private boolean verificarSenha(String senha) {
        if (senha == null) {
            return false;
        }
        byte[] saltBytes = Base64.getDecoder().decode(salt);
        byte[] candidato = Base64.getDecoder().decode(gerarHash(senha, saltBytes));
        byte[] salvo = Base64.getDecoder().decode(senhaHash);
        return java.security.MessageDigest.isEqual(candidato, salvo);
    }

    private static String gerarHash(String senha, byte[] saltBytes) {
        PBEKeySpec specification = new PBEKeySpec(senha.toCharArray(), saltBytes,
                HASH_ITERATIONS, HASH_LENGTH_BITS);
        try {
            byte[] hash = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256")
                    .generateSecret(specification).getEncoded();
            return Base64.getEncoder().encodeToString(hash);
        } catch (GeneralSecurityException exception) {
            throw new IllegalStateException("Não foi possível proteger a senha.", exception);
        } finally {
            specification.clearPassword();
        }
    }

    private static FileStorage<Usuario> storage() {
        return new FileStorage<>(FILE, Usuario.class);
    }
}
