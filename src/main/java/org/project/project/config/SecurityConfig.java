package org.project.project.config;

import org.project.project.service.CustomOAuth2UserService;
import org.project.project.service.CustomOidcUserService;
import org.project.project.service.CustomUserDetailsService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final CustomUserDetailsService userDetailsService;
    private final CustomOAuth2UserService customOAuth2UserService;
    private final CustomOidcUserService customOidcUserService;
    private final OAuth2LoginSuccessHandler oAuth2LoginSuccessHandler;
    private final ImpersonationFilter impersonationFilter;
    private final CustomLoginSuccessHandler customLoginSuccessHandler;
    private final DebugFilter debugFilter;
    private final OAuth2LoginFailureHandler oAuth2LoginFailureHandler;
    private final NoCacheFilter noCacheFilter;

    public SecurityConfig(CustomUserDetailsService userDetailsService,
                          CustomOAuth2UserService customOAuth2UserService,
                          CustomOidcUserService customOidcUserService,
                          OAuth2LoginSuccessHandler oAuth2LoginSuccessHandler,
                          ImpersonationFilter impersonationFilter,
                          CustomLoginSuccessHandler customLoginSuccessHandler,
                          DebugFilter debugFilter,
                          OAuth2LoginFailureHandler oAuth2LoginFailureHandler,
                          NoCacheFilter noCacheFilter) {
        this.userDetailsService = userDetailsService;
        this.customOAuth2UserService = customOAuth2UserService;
        this.customOidcUserService = customOidcUserService;
        this.oAuth2LoginSuccessHandler = oAuth2LoginSuccessHandler;
        this.impersonationFilter = impersonationFilter;
        this.customLoginSuccessHandler = customLoginSuccessHandler;
        this.debugFilter = debugFilter;
        this.oAuth2LoginFailureHandler = oAuth2LoginFailureHandler;
        this.noCacheFilter = noCacheFilter;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public DaoAuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(userDetailsService);
        authProvider.setPasswordEncoder(passwordEncoder());
        return authProvider;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authConfig) throws Exception {
        return authConfig.getAuthenticationManager();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        System.out.println("🔧 [SecurityConfig] Configurando SecurityFilterChain...");

        http.csrf(AbstractHttpConfigurer::disable)
                // Configurar headers de seguridad - permitir iframes del mismo origen
                .headers(headers -> headers
                        .frameOptions(frame -> frame.sameOrigin())
                )
                // CRÍTICO: DebugFilter debe ejecutarse DESPUÉS de SecurityContextHolderFilter
                // para que pueda leer el SecurityContext que fue restaurado desde la sesión
                .addFilterAfter(debugFilter, org.springframework.security.web.context.SecurityContextHolderFilter.class)
                // No-cache filter DESPUÉS de cargar el contexto
                .addFilterAfter(noCacheFilter, org.springframework.security.web.context.SecurityContextHolderFilter.class)
                // Impersonation filter antes de UsernamePasswordAuthenticationFilter
                .addFilterBefore(impersonationFilter, org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter.class)
                .authorizeHttpRequests(auth -> {
                    System.out.println("🛡️ [SecurityConfig] Configurando autorización de requests...");
                    auth.requestMatchers(
                                    "/", "/error", "/signup", "/verify", "/signin", "/sa-access", "/sa-login", 
                                    "/update-password", "/forgot-password", "/verify-code", "/resend-code", 
                                    "/reset-password", "/verify-registration", "/resend-registration-code", 
                                    "/complete-profile", "/auth/finish-profile",
                                    "/invitations/**", // ✅ Aceptar/rechazar invitaciones sin autenticación
                                    "/test/**", "/css/**", "/js/**", "/img/**", "/assets/**", "/webjars/**", 
                                    "/oauth2/**", "/login/oauth2/**", "/login/oauth2/code/**",
                                    "/devportal/user/validate-field",
                                    "/api/reniec/**", // ✅ API pública para validación de DNI con RENIEC
                                    "/api/chatbot/**" // ✅ API pública para chatbot (llamadas desde Cloud Run)
                                    // ⚠️ REMOVIDO: /devportal/** ya NO permite acceso anónimo
                            ).permitAll()
                            .requestMatchers("/devportal/**").authenticated() // 🔒 Requiere autenticación
                            .anyRequest().authenticated();
                })
                .formLogin(form -> {
                    System.out.println("📝 [SecurityConfig] Configurando form login...");
                    form.loginPage("/signin")
                            .loginProcessingUrl("/auth/login") // This should match the form action
                            .usernameParameter("usernameOrEmail")
                            .passwordParameter("password")
                            .successHandler(customLoginSuccessHandler) // Usar handler personalizado
                            .failureUrl("/signin?error=true") // Use a parameter to show error
                            .permitAll();
                })
                .logout(logout -> {
                    System.out.println("🚪 [SecurityConfig] Configurando logout...");
                    logout.logoutUrl("/auth/logout")
                            .logoutSuccessUrl("/signin?logout")
                            .clearAuthentication(true)
                            .invalidateHttpSession(true)
                            .deleteCookies("JSESSIONID")
                            .permitAll();
                })
                .oauth2Login(oauth2 -> {
                    System.out.println("🔐 [SecurityConfig] Configurando OAuth2 login...");
                    System.out.println("🔗 [SecurityConfig] CustomOAuth2UserService: " + customOAuth2UserService);
                    System.out.println("🔗 [SecurityConfig] CustomOidcUserService: " + customOidcUserService);
                    System.out.println("🔗 [SecurityConfig] OAuth2LoginFailureHandler: " + oAuth2LoginFailureHandler);
                    oauth2.loginPage("/signin")
                            .userInfoEndpoint(userInfo -> userInfo
                                    .userService(customOAuth2UserService)
                                    .oidcUserService(customOidcUserService)
                            )
                            .successHandler(oAuth2LoginSuccessHandler)
                            .failureHandler(oAuth2LoginFailureHandler);
                });

        System.out.println("✅ [SecurityConfig] SecurityFilterChain configurado exitosamente");
        return http.build();
    }
}