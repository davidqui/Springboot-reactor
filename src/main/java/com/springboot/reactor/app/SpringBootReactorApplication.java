package com.springboot.reactor.app;

import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.objenesis.instantiator.basic.NewInstanceInstantiator;

import com.springboot.reactor.app.model.Comentarios;
import com.springboot.reactor.app.model.Usuario;
import com.springboot.reactor.app.model.UsuarioComentarios;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@SpringBootApplication
public class SpringBootReactorApplication implements CommandLineRunner{
	
	private static final Logger log = LoggerFactory.getLogger(SpringBootReactorApplication.class);

	public static void main(String[] args) {
		SpringApplication.run(SpringBootReactorApplication.class, args);
	}

	@Override
	public void run(String... args) throws Exception {

		ejemploUsuarioComentariosZipWithForma2();
		
	}
	/**
	 *  Ejemplo combinando flujos con zipWith Forma 2
	 */
	public void ejemploUsuarioComentariosZipWithForma2() {
		Mono<Usuario> usuarioMono = Mono.fromCallable(()
				-> new Usuario("John", "Doe"));

		Mono<Comentarios> comentariosUsuarioMono = Mono.fromCallable(() -> {
			Comentarios comentarios = new Comentarios();
			comentarios.addComentario("Hola pepe, que tal!");
			comentarios.addComentario("Hola pepe, que tal!");
			comentarios.addComentario("Mañana voy a la playa!");
			comentarios.addComentario("Estoy tomomando el curso de spring con reator!");

			return comentarios;
		});

		Mono<UsuarioComentarios> usuarioConComentarios = usuarioMono
				.zipWith(comentariosUsuarioMono)
				.map(tuple -> {
					Usuario u = tuple.getT1();
					Comentarios c = tuple.getT2();

					return new UsuarioComentarios(u, c);
		});
		usuarioConComentarios.subscribe(uc -> log.info(uc.toString()));
	}

	/**
	 *  Ejemplo combinando flujos con FlatMap y  zipWith
	 */
	public void ejemploUsuarioComentariosZipWith() {
		Mono<Usuario> usuarioMono = Mono.fromCallable(()
				-> new Usuario("John", "Doe"));

		Mono<Comentarios> comentariosUsuarioMono = Mono.fromCallable(() -> {
			Comentarios comentarios = new Comentarios();
			comentarios.addComentario("Hola pepe, que tal!");
			comentarios.addComentario("Hola pepe, que tal!");
			comentarios.addComentario("Mañana voy a la playa!");
			comentarios.addComentario("Estoy tomomando el curso de spring con reator!");

			return comentarios;
		});

		Mono<UsuarioComentarios> usuarioComentariosMonoConComentarios = usuarioMono
				.zipWith(comentariosUsuarioMono, ((usuario, comentariosUsuarios) -> new UsuarioComentarios(usuario, comentariosUsuarios)));
		usuarioComentariosMonoConComentarios.subscribe(uc -> log.info(uc.toString()));
	}

	/**
	 * Ejemplo Usuario Comentario con Flatmap
	 */
		public void ejemploUsuarioComentariosFlatMap() {
		Mono<Usuario> usuarioMono = Mono.fromCallable(()
				-> new Usuario("John", "Doe"));
		
		Mono<Comentarios> comentariosUsuarioMono = Mono.fromCallable(() -> {
			Comentarios comentarios = new Comentarios();
			comentarios.addComentario("Hola pepe, que tal!");
			comentarios.addComentario("Hola pepe, que tal!");
			comentarios.addComentario("Mañana voy a la playa!");
			comentarios.addComentario("Estoy tomomando el curso de spring con reator!");
			
			return comentarios;
		});

			Mono<UsuarioComentarios> usuarioComentariosMonoConComentarios = usuarioMono.flatMap(u -> comentariosUsuarioMono.map(c -> new UsuarioComentarios(u, c) ));
			usuarioComentariosMonoConComentarios.subscribe(uc -> log.info(uc.toString()));
	}
	
	public void ejemploCollectList( ) throws Exception {
		List<Usuario> usuariosList = new ArrayList<>();
		usuariosList.add(new Usuario("Andres", " Guzman"));
		usuariosList.add(new Usuario("Pedro", " Fulano"));
		usuariosList.add(new Usuario("Maria", " Mengano"));
		usuariosList.add(new Usuario("Diego", " sultano"));
		usuariosList.add(new Usuario("Juan", " Mengano"));
		usuariosList.add(new Usuario("Bruce", " Lee"));
		usuariosList.add(new Usuario("Bruce", " Willy"));
		
		Flux.fromIterable(usuariosList)
		.collectList()
			.subscribe(lista -> {
				
			lista.forEach(item -> log.info(item.toString()));
			});
	
	}
	
	// Ejemplo toString
	
	public void ejemploToString( ) throws Exception {
		List<Usuario> usuariosList = new ArrayList<>();
		usuariosList.add(new Usuario("Andres", " Guzman"));
		usuariosList.add(new Usuario("Pedro", " Fulano"));
		usuariosList.add(new Usuario("Maria", " Mengano"));
		usuariosList.add(new Usuario("Diego", " sultano"));
		usuariosList.add(new Usuario("Juan", " Mengano"));
		usuariosList.add(new Usuario("Bruce", " Lee"));
		usuariosList.add(new Usuario("Bruce", " Willy"));
		
		Flux.fromIterable(usuariosList)
		.map(usuario -> usuario.getNombre().toUpperCase().concat(" ").concat(usuario.getApellido().toUpperCase()))
				.flatMap(nombre -> {
					if(nombre.contains("bruce".toUpperCase())) {
						return Mono.just(nombre);
					}else {
						return Mono.empty();
					}
						
				})
				.map(nombre -> {
					return nombre.toLowerCase();
				})
				.subscribe(u -> log.info(u.toString()));
					
		
	}
	
	/** Ejemplo ejemploFlatMap */
	
	public void ejemploFlatMap( ) throws Exception {
		List<String> usuariosList = new ArrayList<>();
		usuariosList.add("Andres Guzman");
		usuariosList.add("Pedro Fulano");
		usuariosList.add("Maria Mengano");
		usuariosList.add("Diego sultano");
		usuariosList.add("Juan Mengano");
		usuariosList.add("Bruce Lee");
		usuariosList.add("Bruce Willy");
		
		Flux.fromIterable(usuariosList)
		.map(nombre -> new Usuario(nombre.split(" ")[0].toUpperCase(), nombre.split(" ")[1].toUpperCase()))
				.flatMap(usuario -> {
					if(usuario.getNombre().equalsIgnoreCase("bruce")) {
						return Mono.just(usuario);
					}else {
						return Mono.empty();
					}
						
				})
				.map(usuario -> {
					String nombre = usuario.getNombre().toLowerCase();
					usuario.setNombre(nombre);
					return usuario;
				})
				.subscribe(u -> log.info(u.toString()));
					
		
	}
	
/**	Ejemplo Iterable */
	
	public void ejemploIterable( ) throws Exception {
		List<String> usuariosList = new ArrayList<>();
		usuariosList.add("Andres Guzman");
		usuariosList.add("Pedro Fulano");
		usuariosList.add("Maria Mengano");
		usuariosList.add("Diego sultano");
		usuariosList.add("Juan Mengano");
		usuariosList.add("Bruce Lee");
		usuariosList.add("Bruce Willy");
		
		Flux<String> nombres = Flux.fromIterable(usuariosList); /*Flux.just("Andres Guzman", "Pedro Fulano","Maria Mengano", "Diego sultano", "Juan Mengano", "Bruce Lee", "Bruce Willy");*/
		
		Flux<Usuario> usuarios = nombres.map(nombre -> new Usuario(nombre.split(" ")[0].toUpperCase(), nombre.split(" ")[1].toUpperCase()))
				.filter(usuario -> usuario.getNombre().toLowerCase().equals("bruce"))
				.doOnNext(usuario -> {
					if(usuario == null) {
						throw new RuntimeException("Nombre no puede ser vacio");
					}
					System.out.println(usuario.getNombre().concat(" ").concat(usuario.getApellido()));					
				})
				.map(usuario -> {
					String nombre = usuario.getNombre().toLowerCase();
					usuario.setNombre(nombre);
					return usuario;
				});
		

		usuarios.subscribe(e -> log.info(e.toString()),
				error -> log.error(error.getMessage()),				
				new Runnable() {
					
					@Override
					public void run() {
						log.info("Ha finalizado la ejecucion del observable con exito!");
						
					}
				});
					
		
	}

}
