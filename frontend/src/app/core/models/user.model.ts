export type Role = 'PARENT' | 'CHILD';

export interface User {
  id: string;
  fullName: string;
  username: string;
  email?: string;
  role: Role;
  tokens?: number;
}

export interface AuthResponse {
  token: string;
  user: User;
}
