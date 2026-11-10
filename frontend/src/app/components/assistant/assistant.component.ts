import {Component, inject} from '@angular/core';
import {CommonModule} from '@angular/common';
import {FormsModule} from '@angular/forms';
import {AssistantService} from '../../core/services/assistant.service';
import {RecommendedProduct} from '../../core/models/assistant.model';
import {HttpErrorResponse} from '@angular/common/http';

/**
 * Représente un message échangé dans la conversation avec l'assistant
 */
interface ChatMessage {
  role : 'user' | 'assistant';
  content: string;
  products?: RecommendedProduct[];
}

/**
 * Composant de l'assistant conversationnel de la boutique
 * <p>ce composant permet aux visiteurs et aux utilisateurs de dialoguer
 * avec un assistant ia afin de rechercher des produits dans le catalogue</p>
 * <p>les messages sont affichés sous forme de conversation.
 * les demandes sont transmises au backend via {@link AssistantService},
 * qui se charge de communiquer avec le service d'intelligence artificielle
 * et de récupérer les produits correspondants</p>
 */
@Component({
  selector: 'app-assistant',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './assistant.component.html',
  styleUrl: './assistant.component.css',
})
export class AssistantComponent {
  /** service responsable de l'envoi des messages à l'assistant backend */
  private readonly assistantService = inject(AssistantService);

  /**
   * historique local de la conversation
   */
  messages: ChatMessage[] = [
    {
      role: 'assistant',
      content: "Bonjour ! Je suis votre assistant SpringApp. Que cherchez-vous aujourd'hui ?"
    }
  ];

  /** texte actuellement saisi par l'utilisateur */
  userMessage = "";

  /** indique si une réponse de l'assistant est en cours de traitement */
  isLoading = false;

  /**
   * exemple de demandes proposées pour guider l'utilisateur
   */
  readonly suggestions = [
    "Je cherche un produit à moins de 50€",
    "Quels produits sont disponibles en stock ?",
    "Je cherche un t-shirt"
  ];

  /**
   * Envoie le message saisi à l'assistant
   * <p>la méthode commence par vérifier que le message n'est pas vide
   * et qu'aucune autre requête n'est en cours</p>
   * <p>le message est ajouté à l'historique de conversation, puis
   * transmis au backend via {@link AssistantService}</p>
   * <p>en cas de succès, la résponse textuelle de l'assistant ainsi que
   * les éventuels produits recommandés sont ajoutés à l'historique</p>
   * <p>en cas d'erreur, un message info est affiché à la place de la
   * réponse attendue</p>
   */
  sendMessage(): void {
    const message = this.userMessage.trim();
    if (!message || this.isLoading) {
      return;
    }

    this.messages.push({
      role: 'user',
      content: message
    });

    this.userMessage = '';
    this.isLoading = true;

    this.assistantService.chat(message).subscribe({
      next: response => {
        this.messages.push({
          role: 'assistant',
          content: response.message,
          products: response.products
        });

        this.isLoading = false;
      },
      error: (error: HttpErrorResponse) => {
        let errorMessage = "Une erreur est survenu lors de la communication avec l'assistant";
        if (error.status === 503) {
          errorMessage = "Notre assistant est temporairement indisponible.";
        } else if (error.status === 401) {
          errorMessage = "Votre session n'est plus valide. Veuillez vous reconnecter.";
        } else if (error.status === 0) {
          errorMessage = "Impossible de contacter le serveur";
        }
        this.messages.push({
          role: 'assistant',
          content: errorMessage
        });
        this.isLoading = false;
      }
    });
  }

  /**
   * Envoie une suggestion de recherche prédéfinie
   * <p>la suggestion est placée dans le champ de saisie avant de
   * réutiliser la méthode {@link sendMessage} pour effectuer
   * l'envoi au backend</p>
   * @param suggestion texte de la suggestion
   */
  sendSuggestion(suggestion: string): void {
    if (this.isLoading) {
      return;
    }
    this.userMessage = suggestion;
    this.sendMessage();
  }
}
