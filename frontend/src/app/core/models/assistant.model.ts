export interface ChatRequest {
  message: string;
}

export interface RecommendedProduct {
  id: number;
  name: string;
  price: number;
  available: boolean;
  description: string;
  imageUrl: string | null;
  categoryName: string | null
}

export interface ChatResponse {
  message: string;
  products: RecommendedProduct[];
}
