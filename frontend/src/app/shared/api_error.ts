import {HttpErrorResponse} from '@angular/common/http';

export function apiErrorMessage(
  error: unknown,
  fallback = "Une erreur est survenue, veuillez réessayer plus tard"
): string {
  if (error instanceof HttpErrorResponse) {
    if (error.status === 0) {
      return "Le serveur est injoignable";
    }
    if (error.status === 403) {
      return "Vous n'avez pas les droits d'accès";
    }
    const body = error.error as {detail?: unknown; errors?: Record<string, string>} | null;
    if (body && typeof body === 'object') {
      if (body.errors && typeof body.errors === 'object') {
        const fields = Object.entries(body.errors).map(([field, message]) => `${field} : ${message}`);
        if (fields.length > 0) return fields.join(' . ');
      }
      if (typeof body.detail === 'string' && body.detail.length > 0) {
        return body.detail;
      }
    }
  }
  return fallback;
}
