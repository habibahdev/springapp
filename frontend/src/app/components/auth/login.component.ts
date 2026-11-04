import {Component, inject, signal} from '@angular/core';
import {FormBuilder, ReactiveFormsModule, Validators} from '@angular/forms';
import {Router, RouterLink, ActivatedRoute} from '@angular/router';
import {AuthService} from '../../core/services/auth.service';
import {apiErrorMessage} from '../../shared/api_error';

/**
 * Composant responsable de la gestion du formulaire de connexion.
 * il permet à un utilisateur de s'authentifier à l'aide de son adresse e-mail
 * et de son mot de passe.
 * Le composant assure également :
 * <ul>
 *   <li>la validation des champs du formulaire</li>
 *   <li>l'envoi des identifiants au service d'authentification</li>
 *   <li>l'affichage des erreurs retournées par l'api</li>
 *   <li>la gestion de l'état de chargement pendant la connexion</li>
 *   <li>la redirection de l'utilisateur après une connexion réussie</li>
 * </ul>
 *
 * si une url de redirection est présente dans les paramètres de la route,
 * l'utilisateur y est redirigé après son authentification
 */
@Component({
  selector: 'app-login',
  imports: [ReactiveFormsModule, RouterLink],
  templateUrl: './login.component.html'
})
export class LoginComponent {
  /** service permettant de construire et de gérer le formulaire réactif */
  private readonly formBuilder = inject(FormBuilder);

  /** service responsable de l'authentification des utilisateurs */
  private readonly auth = inject(AuthService);

  /** routeur utilisé pour effectuer les redirections après la connexion */
  private readonly router = inject(Router);

  /** service permettant de récupérer les paramètres de la route actuelle */
  private readonly route = inject(ActivatedRoute);

  /**
   * indique si une requête de connexion est actuellement en cours.
   * permet notamment de gérer l'état de chargement du formulaire
   * pendant l'appel à l'api
   * @protected
   */
  protected readonly submitting = signal(false);

  /**
   * contient le message d'erreur à afficher à l'utilisateur
   * la valeur est null lorsqu'aucune erreur n'est présente.
   * @protected
   */
  protected readonly error = signal<string | null>(null);

  /**
   * formulaire réactif de connexion.
   * iil contient deux champs :
   * <ul>
   *   <li>email : obligatoire et doit respecter le format d'une adresse e-mail</li>
   *   <li>password : obligatoire</li>
   * </ul>
   *
   * la méthode nonNullable.group() garantit que les valeurs des champs
   * ne peuvent pas être null
   * @protected
   */
  protected readonly form = this.formBuilder.nonNullable.group({
    email: ['', [Validators.required, Validators.email]],
    password: ['', [Validators.required]]
  })

  /**
   * vérifie si un champ du formulaire doit être considéré comme invalide
   * pour afficher un message d'erreur.
   * un champ est considéré comme invalide lorsqu'il ne respecte pas ses règles
   * de validation et qu'il a été touché ou modifié par l'utilisateur
   * @param name nom du champ à vérifier
   * @protected
   */
  protected invalid(name: 'email' | 'password'): boolean {
    const control = this.form.controls[name];

    return control.invalid && (control.touched || control.dirty);
  }

  /**
   * soumet le formulaire de connexion.
   * vérifie d'abord la validité du formulaire. si des erreurs
   * sont présentes, tous les champs sont marqués comme touchés
   * afin de rendre visibles les messages de validation.
   * si le formulaire est valide :
   * <ul>
   *   <li>active l'indicateur de chargement</li>
   *   <li>efface les éventuelles erreurs précédentes</li>
   *   <li>transmet les identifiants au service d'authentification</li>
   * </ul>
   * en cas de succès, l'utilisateur est redirigé vers l'url initialement
   * demandée ou vers la page d'accueil si aucune url valide n'est disponible.
   * en cas d'échec, un message d'erreur est récupéré depuis la réponse de l'api
   * et affiché à l'utilisateur
   * @protected
   */
  protected submit(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }
    this.submitting.set(true);
    this.error.set(null);
    this.auth.login(this.form.getRawValue()).subscribe({
      next: () => this.router.navigateByUrl(this.returnUrl()),
      error: (err) => {
        this.error.set(apiErrorMessage(err));
        this.submitting.set(false);
      }
    });
  }

  /**
   * récupère et valide l'url de redirection après la connexion.
   * recherche le paramètre returnUrl dans les paramètres de la route.
   * vérifie que l'url :
   * <ul>
   *   <li>existe</li>
   *   <li>commence par un seul slash (/)</li>
   *   <li>ne commence pas par deux slashs (//), afin d'éviter
   *   une redirection vers un site externe</li>
   * </ul>
   * si l'url ne respecte pas ces conditions, la page d'accueil est utilisée
   * @private
   */
  private returnUrl(): string {
    const url = this.route.snapshot.queryParamMap.get('returnUrl');

    return url && url.startsWith('/') && !url.startsWith('//') ? url : '/';
  }
}
