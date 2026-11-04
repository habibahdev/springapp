import {Component, inject, signal} from '@angular/core';
import {AbstractControl, FormBuilder, ReactiveFormsModule, ValidationErrors, Validators} from '@angular/forms';
import {Router, RouterLink} from '@angular/router';
import {AuthService} from '../../core/services/auth.service';
import {apiErrorMessage} from '../../shared/api_error';

/**
 * Représente les noms des champs disponibles dans le formulaire d'inscription.
 * Ce type permet de limiter les noms de champs utilisables lors de la vérification
 * de leur validité.
 */
type FieldName = 'fullName' | 'email' | 'password' | 'confirmPassword';

/**
 * Validateur personnalisé permettant de vérifier que les deux mots de passe
 * saisis dans le formulaire sont identiques.
 * Récupère les valeurs des champs passwor et confirmPassword, puis
 * compare leurs contenus.
 * Si les deux mots de passe sont différents, une erreur passwordMismatch
 * est retournée
 * @param group groupe de contrôles du formulaire à vérifier
 */
function passwordsMatch(group: AbstractControl): ValidationErrors | null {
  const password = group.get('password')?.value;
  const confirm = group.get('confirmPassword')?.value;

  return password && confirm && password !== confirm ? {passwordMismatch: true} : null;
}

/**
 * Composant responsable de la gestion du formulaire d'inscription.
 * Il permet à un nouvel utilisateur de créer un compte en renseignant :
 * <ul>
 *   <li>son nom complet</li>
 *   <li>son adresse e-mail</li>
 *   <li>son nom mot de passe et sa confirmation</li>
 * </ul>
 *
 * Le composant assure également :
 * <ul>
 *   <li>la validation des champs du formulaire</li>
 *   <li>la vérification de la correspondance des mots de passe</li>
 *   <li>l'envoi des infos au service d'authentification</li>
 *   <li>l'affichage des erreurs retournées par l'api</li>
 *   <li>la redirection vers la page d'accueil après une inscription réussie</li>
 * </ul>
 */
@Component({
  selector: 'app-register',
  imports: [ReactiveFormsModule, RouterLink],
  templateUrl: './register.component.html'
})
export class RegisterComponent {
  /** service permettant de construire et de gérer le formulaire réactif */
  private readonly formBuilder = inject(FormBuilder);

  /** service responsable de l'inscription et de l'authentification des utilisateurs */
  private readonly auth = inject(AuthService);

  /** routeur utilisé pour rediriger l'utilisateur après son inscription */
  private readonly router = inject(Router);

  /**
   * indique si une requête d'inscription est actuellement en cours.
   * permet également de gérer l'état de chargement du formulaire
   * @protected
   */
  protected readonly submitting = signal(false);

  /**
   * contient le message d'erreur à afficher à l'utilisateur.
   * la valeur est null lorsqu'aucune erreur n'est présente
   * @protected
   */
  protected readonly error = signal<string | null>(null);

  /**
   * formulaire réactif d'inscription.
   * chaque champ possède ses propres règles de validation :
   * <ul>
   *   <li>fullName : obligatoire et limité à 120 caractères</li>
   *   <li>email : obligatoire et doit respecter le format d'une adresse e-mail</li>
   *   <li>password : obligatoire, entre 8 et 72 caractères</li>
   *   <li>confirmPassword : obligatoire</li>
   * </ul>
   * Le validateur passwordMatch est appliqué au groupe afin de vérifier que les
   * deux mots de passe sont identiques
   * @protected
   */
  protected readonly form = this.formBuilder.nonNullable.group({
    fullName: ['', [Validators.required, Validators.maxLength(120)]],
    email: ['', [Validators.required, Validators.email]],
    password: ['', [Validators.required, Validators.minLength(8), Validators.maxLength(72)]],
    confirmPassword: ['', Validators.required]
  },
    {validators: passwordsMatch}
  );

  /**
   * Vérifie si un champ du formulaire doit être considéré comme invalide
   * pour afficher un message d'erreur.
   * un champ est considéré comme invalide lorsqu'il ne respecte pas ses règles
   * de validation et qu'il a été modifié ou touché par l'utilisateur
   * @param name nom du champ à vérifier
   * @protected
   */
  protected invalid(name: FieldName): boolean {
    const control = this.form.controls[name];

    return control.invalid && (control.touched || control.dirty);
  }

  /**
   * vérifie si les deux mots de passe sont différents.
   * l'erreur est affichée uniquement lorsque le champ de confirmation
   * du mot de passe a été touché par l'utilisateur
   * @protected
   */
  protected mismatch(): boolean {
    return this.form.hasError('passwordMismatch') && this.form.controls.confirmPassword.touched;
  }

  /**
   * soumet le formulaire d'inscription.
   * vérifie d'abord la validité du formulaire. si des erreurs sont présents,
   * tous les champs sont marqués comme touchés afin de rendre visibles les
   * messages de validation.
   * si le formulaire est valide :
   * <ul>
   *   <li>active l'indicateur de chargement</li>
   *   <li>efface les éventuelles erreurs précédentes</li>
   *   <li>récupère les informations nécessaires à l'inscription</li>
   *   <li>appelle le service d'authentification</li>
   * </ul>
   *
   * en cas d'échec, un message d'erreur est récupéré depuis la réponse de l'api.
   * en cas de succès, l'utilisateur est redirigé vers la page d'accueil
   * @protected
   */
  protected submit(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }
    this.submitting.set(true);
    this.error.set(null);

    const {fullName, email, password} = this.form.getRawValue();
    this.auth.register({fullName, email, password}).subscribe({
      error: (err) => {
        this.error.set(apiErrorMessage(err));
        this.submitting.set(false);
      },
      next: () => this.router.navigateByUrl('/')
    });
  }
}
