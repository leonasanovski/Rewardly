export interface Child {
  id: string;
  fullName: string;
  username: string;
  tokens: number;
}

export interface CreateChildRequest {
  fullName: string;
  username: string;
  password: string;
}

export interface UpdateChildRequest {
  fullName?: string;
  username?: string;
  password?: string;
}
