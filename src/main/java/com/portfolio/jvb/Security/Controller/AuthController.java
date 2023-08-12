/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.portfolio.jvb.Security.Controller;

import com.portfolio.jvb.Security.Dto.JwtDto;
import com.portfolio.jvb.Security.Dto.LoginUsuario;
import com.portfolio.jvb.Security.Dto.NuevoUsuario;
import com.portfolio.jvb.Security.Entity.Rol;
import com.portfolio.jvb.Security.Entity.Usuario;
import com.portfolio.jvb.Security.Enums.RolNombre;
import com.portfolio.jvb.Security.jwt.JwtProvider;
import com.portfolio.jvb.Security.Service.RolService;
import com.portfolio.jvb.Security.Service.UsuarioService;
import java.util.HashSet;
import java.util.Set;
import javax.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.neo4j.Neo4jProperties.Authentication;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;




@RestController
@RequestMapping("/auth")
@CrossOrigin(origins = {"https://frontend-44d67.web.app","http://localhost:4200"})
public class AuthController {
    @Autowired
    PasswordEncoder passwordEncoder;
    @Autowired
    AuthenticationManager authenticationManager;
    @Autowired
    UsuarioService usuarioService;
    @Autowired
    RolService rolService;
    @Autowired
    JwtProvider jwtProvider;
    
 @PostMapping("/nuevo")
 public ResponseEntity<?>nuevo(@Valid @RequestBody NuevoUsuario nuevoUsuario, BindingResult bindingResult){
     if(bindingResult.hasErrors())
         return new ResponseEntity(new Mensaje("campos mal puestos o mail invalido"),HttpStatus.BAD_REQUEST);
     
     if(usuarioService.existsByNombreUsuario(nuevoUsuario.getNombreUsuario()))
         return new ResponseEntity(new Mensaje("nombre de usuario ya en uso"),HttpStatus.BAD_REQUEST);
     if(usuarioService.existsByEmail(nuevoUsuario.getEmail()))
         return new ResponseEntity(new Mensaje("email ya registrado"),HttpStatus.BAD_REQUEST);
     Usuario usuario = new Usuario(nuevoUsuario.getNombre(),nuevoUsuario.getNombreUsuario(),nuevoUsuario.getEmail(),
     passwordEncoder.encode(nuevoUsuario.getPassword()));
     
     Set<Rol> roles = new HashSet<>();
        roles.add(rolService.getByRol(RolNombre.ROLE_ADMIN).get());
        usuario.setRoles(roles);
        usuarioService.save(usuario);
        return new ResponseEntity(new Mensaje("Usuario Creado"), HttpStatus.CREATED);
 }
 @PostMapping("/login")
 public ResponseEntity<JwtDto> login(@Valid @RequestBody LoginUsuario loginUsuario, BindingResult bindingResult){
     if(bindingResult.hasErrors())
         return new ResponseEntity(new Mensaje("campos mal puestos"),HttpStatus.BAD_REQUEST);
     org.springframework.security.core.Authentication authentication = authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(
            loginUsuario.getNombreUsuario(), loginUsuario.getPassword()));
     
     SecurityContextHolder.getContext().setAuthentication(authentication);
     String jwt =jwtProvider.generateToken(authentication);
     UserDetails userDetails =(UserDetails) authentication.getPrincipal();
     JwtDto jwtDto= new JwtDto(jwt, userDetails.getUsername(),userDetails.getAuthorities());
     return new ResponseEntity(jwtDto, HttpStatus.OK);
 }
}
