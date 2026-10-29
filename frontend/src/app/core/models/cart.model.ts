export interface CartLine {
  productId: number;
  name: string;
  price: number;
  imageUrl: string | null;
  stock: number;
  quantity: number;
}
