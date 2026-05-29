import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Router } from '@angular/router';
import { tap } from 'rxjs/operators';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { AuthResponse } from '../models/user.model';
import { TokenStorageService } from './token-storage.service';

@Injectable({ providedIn: 'root' })
export class AuthService {
  private readonly base = `${environment.apiUrl}/auth`;

  constructor(
    private http: HttpClient,
    private tokenStorage: TokenStorageService,
    private router: Router
  ) {}

  login(identifier: string, password: string): Observable<AuthResponse> {
    return this.http.post<AuthResponse>(`${this.base}/login`, { identifier, password }).pipe(
      tap(res => {
        this.tokenStorage.saveToken(res.token);
        this.tokenStorage.saveUser(res.user);
      })
    );
  }

  register(payload: {
    fullName: string;
    email: string;
    password: string;
    embg: string;
  }): Observable<AuthResponse> {
    return this.http.post<AuthResponse>(`${this.base}/register`, payload).pipe(
      tap(res => {
        this.tokenStorage.saveToken(res.token);
        this.tokenStorage.saveUser(res.user);
      })
    );
  }

  logout(): void {
    this.tokenStorage.clear();
    this.router.navigate(['/auth/login']);
  }

  isLoggedIn(): boolean {
    return !!this.tokenStorage.getToken();
  }

  getRole(): string | null {
    return this.tokenStorage.getUser()?.role ?? null;
  }
}
