package com.micompra.micompraya.services;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseToken;
import com.micompra.micompraya.models.*;
import com.micompra.micompraya.repositories.ClienteRepository;
import lombok.RequiredArgsConstructor;
import org.mindrot.jbcrypt.BCrypt;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.micompra.micompraya.repositories.UsuarioRepository;
import com.micompra.micompraya.repositories.EstadoRepository;
import com.micompra.micompraya.repositories.RolRepository;
import com.micompra.micompraya.models.Usuario;
import org.springframework.util.Assert;


import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

@Service
@Transactional
@RequiredArgsConstructor
public class UsuarioService {

    private final  UsuarioRepository usuarioRepo;
    private final RolRepository rolRepo;
    private final EstadoRepository estadoRepo;
    private final EmailService emailService;
    private final ClienteService clienteService;
    private final ClienteRepository clienteRepo;

    //listar todos los usuarios.
    @Transactional(readOnly = true)
    public List<Usuario> listarUsuarios()
    {
        return  usuarioRepo.findAll();
    }

    @Transactional(readOnly = true)
    public List<Usuario> listarUsuariosActivos() {
        return usuarioRepo.findUsuariosActivos();
    }


    //Buscar usuario por email
    @Transactional(readOnly = true)
    public Optional<Usuario> buscarUsuarioPorEmail(String correo){
        Assert.hasText(correo, "El correo es incorrecto");
        return  usuarioRepo.findByCorreo(correo);
    }

    //Eliminar usuario por id
    public void eliminarUsuario(Integer id){
        Assert.notNull(id, "El id es requerido");
        if (!usuarioRepo.existsById(id)){
            throw new  IllegalArgumentException("El usuario no existe");
        }
        usuarioRepo.deleteById(id);
    }

    //Cambiar contraseña de usuario

    //buscar usuario por id
    @Transactional(readOnly = true)
    public Optional<Usuario> buscarUsuarioPorId(Integer id){
        Assert.notNull(id, "El id es requerido");
        return usuarioRepo.findById(id);

    }

    //Version 2
    @Transactional(readOnly = true)
    public Usuario obtenerUsuarioPorId(Integer id) {
        Assert.notNull(id, "El id es requerido");
        return usuarioRepo.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado"));
    }


    //Metodos privados que se ocuparan varias veces para validar datos

    //verificar el usuario no sea repetido
    private void validarUsuario(String usuario){
        Assert.hasText(usuario, "El usuario es requerido");
        if (usuarioRepo.findByNombreUsuario(usuario).isPresent()){
            throw new IllegalArgumentException("El usuario "+ usuario + " ya existe");
        }
    }
    //verificar el correo no sea repetido
    private void validarCorreo(String correo){
        Assert.hasText(correo, "El correo es requerido");
        if (usuarioRepo.findByCorreo(correo).isPresent()){
            throw new IllegalArgumentException("El correo "+ correo + " ya existe");
        }
    }

    //Verificar el rol exista por id
    private void validarRol(Integer id){
        Assert.notNull(id, "El id es requerido");
        if (!rolRepo.existsById(id)){
            throw new IllegalArgumentException("El rol no existe");
        }
    }
    //Verificar el estado exista por id
    private void validarEstado(Integer id){
        Assert.notNull(id, "El id es requerido");
        if (!estadoRepo.existsById(id)){
            throw new IllegalArgumentException("El estado no existe");
        }
    }


//    //crear usuario nuevo con validaciones
//    public int crearUsuario(Usuario usuario){
//
//        if (usuarioRepo.existsByNombreUsuario(usuario.getNombreUsuario())){
//            return 2;
//        }
//        if (usuarioRepo.existsByCorreo(usuario.getCorreo())){
//            return 3;
//        }
//        usuario.setFechaRegistro(LocalDate.now());
//        usuario.setEstado(estadoRepo.findById(1).get());
//
//        //crear contraseña
//        String contrasenaPlano = generarContrasena(8);
//        String contrasenaHasheada = hashearContrasena(contrasenaPlano);
//        usuario.setContrasena(contrasenaHasheada);
//
//        usuarioRepo.save(usuario);
//    }

    //Agregar un usuario nuevo con validaciones
//Agregar un usuario nuevo con validaciones
    public Usuario agregarUsuario(Usuario usuario) {
        Assert.notNull(usuario, "El usuario es requerido");
        Assert.hasText(usuario.getNombreUsuario(), "El usuario es requerido");
        Assert.hasText(usuario.getCorreo(), "El correo es requerido");
        Assert.notNull(usuario.getRol(), "El rol es requerido");
        Assert.notNull(usuario.getRol().getId(), "El ID de Rol es requerido");



        validarUsuario(usuario.getNombreUsuario());
        validarCorreo(usuario.getCorreo());

        // --- 2. LÓGICA MOVIDA DEL CONTROLADOR ---
        // Buscamos las entidades completas aquí, en el servicio
        Rol rol = rolRepo.findById(usuario.getRol().getId())
                .orElseThrow(() -> new IllegalArgumentException("El rol con ID " + usuario.getRol().getId() + " no existe"));
        Integer estadoId = (usuario.getEstado() != null && usuario.getEstado().getId() != null) ? usuario.getEstado().getId() : 1;

        Estado estado = estadoRepo.findById(estadoId)
                .orElseThrow(() -> new IllegalArgumentException("El estado con ID " + estadoId + " no existe"));

        usuario.setEstado(estado);

        usuario.setRol(rol);
        usuario.setEstado(estado);
        // --- FIN LÓGICA MOVIDA ---

        //Generar y encriptar contraseña
        String contrasenaPlano = generarContrasena(8);

        emailService.sendEmail(
                usuario.getCorreo(),
                "Nueva contraseña generada - MiCompraYA",
                "Hola " + usuario.getNombreUsuario() + ",\n\n" +
                        "Hemos generado una nueva contraseña temporal para tu cuenta:\n\n" +
                        "👉 " + contrasenaPlano + "\n\n" +
                        "Por tu seguridad, te recomendamos iniciar sesión lo antes posible y cambiar esta contraseña desde la sección de configuración de tu cuenta.\n\n" +
                        "¡Gracias por confiar en MiCompraYA!\n" +
                        "Atentamente,\n" +
                        "El equipo de MiCompraYA"
        );

        usuario.setContrasena(BCrypt.hashpw(contrasenaPlano, BCrypt.gensalt()));

        // El servicio también se encarga de la fecha de registro
        if (usuario.getFechaRegistro() == null) {
            usuario.setFechaRegistro(LocalDate.now());
        }
        return usuarioRepo.save(usuario);
    }

    //Actualizar un usuario
    public Usuario actualizarUsuario(Usuario usuario, Integer id_usuario) {
        Assert.notNull(usuario, "El usuario es requerido");
        Assert.notNull(id_usuario, "El id del usuario es requerido");
        Assert.hasText(usuario.getNombreUsuario(), "El usuario es requerido");
        Assert.hasText(usuario.getCorreo(), "El correo es requerido");
        Assert.notNull(usuario.getRol(), "El rol es requerido");
        Assert.notNull(usuario.getRol().getId(), "El ID de Rol es requerido");
        Assert.notNull(usuario.getEstado(), "El estado es requerido");
        Assert.notNull(usuario.getEstado().getId(), "El ID de Estado es requerido");

        Usuario usuarioActual = usuarioRepo.findById(id_usuario)
                .orElseThrow(() -> new IllegalArgumentException("El usuario no existe"));

        // Validar que el nombre no esté usado por otro usuario
        Optional<Usuario> usuarioConMismoNombre = usuarioRepo.findByNombreUsuario(usuario.getNombreUsuario());
        if (usuarioConMismoNombre.isPresent() && !usuarioConMismoNombre.get().getId().equals(id_usuario)) {
            throw new IllegalArgumentException("El nombre de usuario ya está en uso por otro usuario.");
        }

        // Validar que el correo no esté usado por otro usuario
        Optional<Usuario> usuarioConMismoCorreo = usuarioRepo.findByCorreo(usuario.getCorreo());
        if (usuarioConMismoCorreo.isPresent() && !usuarioConMismoCorreo.get().getId().equals(id_usuario)) {
            throw new IllegalArgumentException("El correo ya está en uso por otro usuario.");
        }

        // --- 2. LÓGICA MOVIDA DEL CONTROLADOR ---
        // Buscamos las entidades completas aquí
        Rol rol = rolRepo.findById(usuario.getRol().getId())
                .orElseThrow(() -> new IllegalArgumentException("El rol con ID " + usuario.getRol().getId() + " no existe"));
        Estado estado = estadoRepo.findById(usuario.getEstado().getId())
                .orElseThrow(() -> new IllegalArgumentException("El estado con ID " + usuario.getEstado().getId() + " no existe"));
        // --- FIN LÓGICA MOVIDA ---

        // Actualizar solo los datos permitidos
        usuarioActual.setNombreUsuario(usuario.getNombreUsuario());
        usuarioActual.setCorreo(usuario.getCorreo());
        usuarioActual.setRol(rol); // <-- Usamos las entidades cargadas
        usuarioActual.setEstado(estado); // <-- Usamos las entidades cargadas

        return usuarioRepo.save(usuarioActual);
    }
    public String generarContrasena(int longitud) {
        String caracteres = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789@#$%&*!?";
        SecureRandom random = new SecureRandom();
        StringBuilder sb = new StringBuilder();

        for (int i = 0; i < longitud; i++) {
            int index = random.nextInt(caracteres.length());
            sb.append(caracteres.charAt(index));
        }

        return sb.toString();
    }

    public String hashearContrasena(String contrasena) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(contrasena.getBytes());
            return Base64.getEncoder().encodeToString(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Error al hashear la contraseña", e);
        }
    }


    //autenticar usuario
    public Usuario autenticarUsuario(String usuario, String contrasena){
        Optional<Usuario> usuarioOptional = usuarioRepo.findByNombreUsuario(usuario);
        if(usuarioOptional.isPresent() && BCrypt.checkpw(contrasena, usuarioOptional.get().getContrasena()) && usuarioOptional.get().getEstado().getId() == 1){

            return usuarioOptional.get();
        }
        return null;
    }

    //cambiar contrasena
    public void cambiarContrasena(String confirmar, Integer idUsuario){
        Usuario usuario = usuarioRepo.findById(idUsuario).get();
        usuario.setContrasena(BCrypt.hashpw(confirmar, BCrypt.gensalt()));
        usuarioRepo.save(usuario);
    }



    public Usuario agregarUsuarioYRetornarId(Usuario usuario) {
        // Validaciones básicas
        Assert.notNull(usuario, "El usuario es requerido");
        Assert.hasText(usuario.getNombreUsuario(), "El nombre de usuario es requerido");
        Assert.hasText(usuario.getCorreo(), "El correo es requerido");
        Assert.notNull(usuario.getRol(), "El rol es requerido");
        Assert.notNull(usuario.getEstado(), "El estado es requerido");

        // Validar unicidad y existencia de rol/estado
        validarUsuario(usuario.getNombreUsuario());
        validarCorreo(usuario.getCorreo());
        validarRol(usuario.getRol().getId());
        validarEstado(usuario.getEstado().getId());

        // Generar contraseña temporal
        String contrasenaPlano = generarContrasena(8);

        // Enviar correo con contraseña temporal
        emailService.sendEmail(
                usuario.getCorreo(),
                "Nueva contraseña generada - MiCompraYA",
                "Hola " + usuario.getNombreUsuario() + ",\n\n" +
                        "Hemos generado una nueva contraseña temporal para tu cuenta:\n\n" +
                        "👉 " + contrasenaPlano + "\n\n" +
                        "Por tu seguridad, te recomendamos iniciar sesión lo antes posible y cambiar esta contraseña.\n\n" +
                        "¡Gracias por confiar en MiCompraYA!\n" +
                        "El equipo de MiCompraYA"
        );

        // Encriptar la contraseña
        usuario.setContrasena(BCrypt.hashpw(contrasenaPlano, BCrypt.gensalt()));

        // Fecha de registro por defecto
        if (usuario.getFechaRegistro() == null) {
            usuario.setFechaRegistro(LocalDate.now());
        }

        // Guardar usuario en la base de datos y retornar el objeto persistido
        Usuario usuarioGuardado = usuarioRepo.save(usuario);

        // Ahora puedes acceder a usuarioGuardado.getId() para asociarlo con Cliente
        return usuarioGuardado;
    }

    // --- 3. NUEVO MÉTODO DE SERVICIO ---
    // Toda la lógica de registro de cliente movida aquí
    public void registrarNuevoCliente(String nombreCompleto, String nombreUsuario, String correo, String telefono, String direccion) {
        // La anotación @Transactional de la clase se encargará del rollback si algo falla

        // 1️⃣ Crear el usuario
        Usuario nuevoUsuario = new Usuario();
        nuevoUsuario.setNombreUsuario(nombreUsuario);
        nuevoUsuario.setCorreo(correo);
        nuevoUsuario.setFechaRegistro(LocalDate.now());

        // Rol y estado por defecto (usando nuestros repositorios)
        // Asegúrate que los IDs 2 (Cliente) y 1 (Activo) sean correctos en tu DB
        Rol rol = rolRepo.findById(2)
                .orElseThrow(() -> new RuntimeException("Rol 'Cliente' (ID 3) no encontrado. Verifica la DB."));
        Estado estado = estadoRepo.findById(1)
                .orElseThrow(() -> new RuntimeException("Estado 'Activo' (ID 1) no encontrado. Verifica la DB."));
        nuevoUsuario.setRol(rol);
        nuevoUsuario.setEstado(estado);

        // Guardar usuario (esto ya genera pass, envía email y retorna el usuario con ID)
        Usuario usuarioGuardado = this.agregarUsuarioYRetornarId(nuevoUsuario);

        if (usuarioGuardado == null || usuarioGuardado.getId() == null) {
            throw new RuntimeException("Error al crear el usuario, no se obtuvo ID.");
        }

        // 2️⃣ Crear el cliente asociado
        Cliente nuevoCliente = new Cliente();
        nuevoCliente.setNombreCompleto(nombreCompleto);
        nuevoCliente.setTelefono(telefono);
        nuevoCliente.setDireccion(direccion);
        nuevoCliente.setUsuario(usuarioGuardado);

        // Guardar cliente
        clienteService.agregarCliente(nuevoCliente);

        // 3️⃣ Si todo sale bien, la transacción hace commit.
        // Si algo falla (ej: rol no existe, email duplicado), la transacción hace rollback.
    }

    //eliminar usuario(ponerlo inactivo el estado)
    public void inactivarUsuario(Integer id) {
        Assert.notNull(id, "El id es requerido");
        Usuario usuario = usuarioRepo.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado"));

        // Buscar el estado "Inactivo" (ajusta según tus IDs)
        Estado estadoInactivo = estadoRepo.findById(2)
                .orElseThrow(() -> new IllegalArgumentException("Estado 'Inactivo' no encontrado"));

        usuario.setEstado(estadoInactivo);
        usuarioRepo.save(usuario);
    }


    public void recuperarContrasenaPorCorreo(String correo) {
        Assert.hasText(correo, "El correo no puede estar vacío.");

        // 1. Buscar al usuario por correo
        Usuario usuario = usuarioRepo.findByCorreo(correo)
                .orElseThrow(() -> new IllegalArgumentException("El correo electrónico ingresado no está registrado."));

        // 2. Generar nueva contraseña temporal
        String nuevaContrasenaPlano = generarContrasena(8); // Reutiliza tu método existente

        // --- CAMBIO DE ORDEN ---
        // 3. Enviar PRIMERO el correo con la contraseña en texto plano
        try {
            emailService.sendEmail(
                    usuario.getCorreo(),
                    "Recuperación de Contraseña - MiCompraYA",
                    "Hola " + usuario.getNombreUsuario() + ",\n\n" +
                            "Hemos generado una nueva contraseña temporal para tu cuenta:\n\n" +
                            "👉 " + nuevaContrasenaPlano + "\n\n" +
                            "Por favor, inicia sesión con esta contraseña y cámbiala lo antes posible desde la configuración de tu cuenta.\n\n" +
                            "Si no solicitaste esto, puedes ignorar este correo.\n\n" +
                            "Atentamente,\n" +
                            "El equipo de MiCompraYA"
            );
        } catch (Exception e) {
            // Si falla el envío del correo, lanzamos una excepción.
            // Gracias a @Transactional, esto evitará que se guarde la nueva contraseña.
            throw new RuntimeException("Error al enviar el correo de recuperación. La contraseña no ha sido cambiada.", e);
        }

        // --- CONTINÚA SI EL CORREO SE ENVIÓ CORRECTAMENTE ---
        // 4. Hashear la nueva contraseña
        String nuevaContrasenaHasheada = BCrypt.hashpw(nuevaContrasenaPlano, BCrypt.gensalt());

        // 5. Actualizar la contraseña en el objeto Usuario
        usuario.setContrasena(nuevaContrasenaHasheada);

        // 6. Guardar los cambios en la base de datos (SOLO si el correo se envió)
        usuarioRepo.save(usuario);
    }

    @Transactional(readOnly = true) // Es una consulta, no modifica nada
    public boolean existeCorreo(String correo) {
        if (correo == null || correo.isBlank()) {
            return false; // No consideramos correos vacíos como existentes
        }
        // findByCorreo devuelve un Optional<Usuario>. isPresent() nos dice si encontró algo.
        return usuarioRepo.findByCorreo(correo).isPresent();
    }


    public Map<String, Object> procesarLoginGoogle(String idToken) throws Exception {
        Map<String, Object> resultado = new HashMap<>();

        // 1. Lógica de Negocio: Verificar con Firebase (Integración externa)
        FirebaseToken decodedToken = FirebaseAuth.getInstance().verifyIdToken(idToken);
        String email = decodedToken.getEmail();
        String nombre = decodedToken.getName();

        // 2. Lógica de Negocio: Verificar existencia en BD
        Optional<Usuario> usuarioOpt = usuarioRepo.findByCorreo(email);

        if (usuarioOpt.isPresent()) {
            Usuario usuario = usuarioOpt.get();

            // Regla de Negocio: Validar estado
            if (usuario.getEstado().getId() != 1) {
                resultado.put("status", "error");
                resultado.put("message", "Usuario inactivo. Contacte al administrador.");
                return resultado;
            }

            // Usuario válido encontrado
            resultado.put("status", "success");
            resultado.put("usuarioEncontrado", usuario); // Pasamos la entidad al controlador
            resultado.put("redirect", "/home");

        } else {
            // Usuario nuevo
            resultado.put("status", "new_user");
            resultado.put("googleEmail", email);
            resultado.put("googleNombre", nombre);
            resultado.put("redirect", "/completar-registro");
        }

        return resultado;
    }

    public Map<String, Object> obtenerDatosPerfilCompleto(Integer usuarioId) {
        Usuario usuario = usuarioRepo.findById(usuarioId)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        // Buscar datos de cliente si existen
        Optional<Cliente> clienteOpt = clienteRepo.findByUsuario_Id(usuarioId);

        Map<String, Object> datos = new HashMap<>();
        datos.put("nombreUsuario", usuario.getNombreUsuario());
        datos.put("correo", usuario.getCorreo());

        if (clienteOpt.isPresent()) {
            Cliente cliente = clienteOpt.get();
            datos.put("nombreCompleto", cliente.getNombreCompleto());
            datos.put("telefono", cliente.getTelefono());
            datos.put("direccion", cliente.getDireccion());
        } else {
            datos.put("nombreCompleto", "");
            datos.put("telefono", "");
            datos.put("direccion", "");
        }
        return datos;
    }

    @Transactional
    public Usuario actualizarPerfilCompleto(Integer usuarioId, Map<String, String> datos) {
        Usuario usuario = usuarioRepo.findById(usuarioId)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        String nuevoUsuario = datos.get("nombreUsuario");
        String nuevoCorreo = datos.get("correo");

        // Validar unicidad solo si cambiaron
        if (!usuario.getNombreUsuario().equals(nuevoUsuario)) {
            validarUsuario(nuevoUsuario); // Tu método privado existente
            usuario.setNombreUsuario(nuevoUsuario);
        }
        if (!usuario.getCorreo().equals(nuevoCorreo)) {
            validarCorreo(nuevoCorreo); // Tu método privado existente
            usuario.setCorreo(nuevoCorreo);
        }

        // Actualizar datos de Cliente
        Optional<Cliente> clienteOpt = clienteRepo.findByUsuario_Id(usuarioId);
        Cliente cliente;
        if (clienteOpt.isPresent()) {
            cliente = clienteOpt.get();
        } else {
            // Si era un admin y quiere agregar datos de cliente ahora
            cliente = new Cliente();
            cliente.setUsuario(usuario);
        }

        cliente.setNombreCompleto(datos.get("nombreCompleto"));
        cliente.setTelefono(datos.get("telefono"));
        cliente.setDireccion(datos.get("direccion"));

        clienteRepo.save(cliente);
        return usuarioRepo.save(usuario); // Retornamos usuario actualizado
    }

}
