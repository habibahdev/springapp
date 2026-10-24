package com.habibah.springapp.exception;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Gestionnaire global des exceptions des contrôleurs REST.
 * <p>Intercept certaines exceptions levées pendant le traitement des requêtes
 * http et les transforme en réponses structurées au format {@link ProblemDetail}</p>
 * <p>Ce gestionnaire centralise les réponses associées aux erreurs métier,
 * aux ressources introuvables, aux données invalides, aux identifiants incorrects
 * et aux violations des contraintes d'intégrité des données.</p>
 */
@RestControllerAdvice
public class GlobalExceptionHandler {
    /**
     * Traite les exceptions indiquant qu'une ressource est introuvable.
     * @param ex exception contenant le détail de l'erreur
     * @return une réponse http 404 avec le message de l'exception
     */
    @ExceptionHandler(NotFoundException.class)
    public ProblemDetail handleNotFound(NotFoundException ex) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, ex.getMessage());
    }

    /**
     * Traite les exceptions correspondant à une erreur métier
     * @param ex exception contenant le détail du problème métier
     * @return une réponse http 409 avec le message de l'exception
     */
    @ExceptionHandler(BusinessException.class)
    public ProblemDetail handleBusinessException(BusinessException ex) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT, ex.getMessage());
    }

    /**
     * Traite les erreurs de validation des paramètres d'une requête.
     * <p>Récupère les erreurs de validation associées aux champs et les regroupe
     * dans une map. Le nom de chaque champ est associé à son message d'erreur.</p>
     * @param ex exception contenant les résultats de validation
     * @return une réponse http 400 contenant un message général et une propriété
     * {@code errors} détaillant les erreurs rencontrées
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ProblemDetail handleValidation(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new LinkedHashMap<>();
        ex.getBindingResult().getFieldErrors()
                .forEach(e -> errors.put(e.getField(), e.getDefaultMessage()));
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, "Données invalides");
        problemDetail.setProperty("errors", errors);
        return problemDetail;
    }

    /**
     * Traite les erreurs d'authentification dues à des identifiants incorrects.
     * @param ex exception signalant l'échec de l'authentification
     * @return une réponse http 401 avec un message générique
     */
    @ExceptionHandler(BadCredentialsException.class)
    public ProblemDetail handleBadCredentials(BadCredentialsException ex) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.UNAUTHORIZED, "Email ou mot de passe incorrect");
    }

    /**
     * Traite les violations des contraintes d'intégrité des données.
     * <p>Cette erreur peut notamment survenir lorsqu'une contrainte d'unicité
     * est violée ou lorsqu'une opération entre en conflit avec une contrainte
     * de la base de données.</p>
     * @param ex exception liée à une violation d'intégrité
     * @return une réponse http 409 avec un message générique
     */
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ProblemDetail handleIntegrity(DataIntegrityViolationException ex) {
        return ProblemDetail.forStatusAndDetail(
                HttpStatus.CONFLICT,
                "Opération impossible : cet élement est utilisé ailleurs ou existe déjà");
    }
}
