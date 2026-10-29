export interface Product {
  id: number;
  name: string;
  description: string | null;
  price: number;
  stock: number;
  imageUrl: string | null;
  categoryId: number | null;
  categoryName: string | null;
}

export interface ProductRequest {
  name: string;
  description?: string | null;
  price: number;
  stock: number;
  imageUrl?: string | null;
  categoryId?: number | null;
  categoryName?: string | null;
}

export interface ProductQuery {
  categoryId?: number;
  q?: string;
  page?: number;
  size?: number;
  sort?: 'name' | 'price' | 'id';
}

export interface Page<T> {
  content: T[];
  totalElements: number;
  totalPages: number;
  number: number;
  size: number;
  first: boolean;
  last: boolean;
}
