import { Injectable } from '@angular/core';
import { BehaviorSubject } from 'rxjs';
import { User } from '../models/user.model';

const TOKEN_KEY = 'fq_token';
const USER_KEY = 'fq_user';

@Injectable({ providedIn: 'root' })
export class TokenStorageService {
  private readonly userSubject: BehaviorSubject<User | null>;
  readonly user$;

  constructor() {
    const stored = localStorage.getItem(USER_KEY);
    this.userSubject = new BehaviorSubject<User | null>(stored ? JSON.parse(stored) : null);
    this.user$ = this.userSubject.asObservable();
  }

  getToken(): string | null {
    return localStorage.getItem(TOKEN_KEY);
  }

  saveToken(token: string): void {
    localStorage.setItem(TOKEN_KEY, token);
  }

  getUser(): User | null {
    return this.userSubject.value;
  }

  saveUser(user: User): void {
    localStorage.setItem(USER_KEY, JSON.stringify(user));
    this.userSubject.next(user);
  }

  updateTokens(tokens: number): void {
    const user = this.getUser();
    if (user) this.saveUser({ ...user, tokens });
  }

  clear(): void {
    localStorage.removeItem(TOKEN_KEY);
    localStorage.removeItem(USER_KEY);
    this.userSubject.next(null);
  }
}
