export interface User {
  uuid: string;
  username: string;
  role: UserRole;
  active: boolean;
}

export type UserRole = 'ROLE_MANAGER' | 'ROLE_CASHIER' | 'ROLE_STOCK' | 'ROLE_FINANCE';

export interface TokenPair {
  accessToken: string;
  refreshToken: string;
  expiresIn: number;
}

export interface LoginCredentials {
  username: string;
  password: string;
}

export interface RegisterData {
  fullName: string;
  email: string;
  password: string;
  phone: string;
  cpf?: string;
}
