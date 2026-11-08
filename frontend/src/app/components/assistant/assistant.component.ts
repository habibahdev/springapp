import {Component, inject} from '@angular/core';
import {CommonModule} from '@angular/common';
import {FormsModule} from '@angular/forms';
import {AssistantService} from '../../core/services/assistant.service';
import {RecommendedProduct} from '../../core/models/assistant.model';

interface ChatMessage {
  role : 'user' | 'assistant';
  content: string;
  products?: RecommendedProduct[];
}

@Component({
  selector: 'app-assistant',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './assistant.component.html',
  styleUrl: './assistant.component.css',
})
export class AssistantComponent {
  private readonly assistantService = inject(AssistantService);

  messages: ChatMessage[] = [
    {
      role: 'assistant',
      content: "Bonjour ! Je suis votre assistant SpringApp. Que cherchez-vous aujourd'hui ?"
    }
  ];

  userMessage = "";

  isLoading = false;

  readonly suggestions = [
    "Je cherche un produit à moins de 50€",
    "Quels produits sont disponibles en stock ?",
    "Je cherche un t-shirt"
  ];

  /**
   * Envoie le message saisi au backend
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
      error: () => {
        this.messages.push({
          role: 'assistant',
          content: "Une erreur est survenu lors de la communication avec l'assistant"
        });
        this.isLoading = false;
      }
    });
  }

  /**
   * Envoie une suggestion de recherche prédéfinie
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
