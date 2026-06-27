export interface ProductImage {
  id: number;
  filename: string;
  originalName: string;
  contentType: string;
  fileSize: number;
  sortOrder: number;
  main: boolean;
  createdAt: string;
  thumbnailUrl: string;
  cardUrl: string;
  fullUrl: string;
}
