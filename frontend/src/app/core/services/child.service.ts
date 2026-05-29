import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { Child, CreateChildRequest, UpdateChildRequest } from '../models/child.model';

@Injectable({ providedIn: 'root' })
export class ChildService {
  private readonly base = `${environment.apiUrl}/children`;

  constructor(private http: HttpClient) {}

  getChildren(): Observable<Child[]> {
    return this.http.get<Child[]>(this.base);
  }

  createChild(data: CreateChildRequest): Observable<Child> {
    return this.http.post<Child>(this.base, data);
  }

  updateChild(id: string, data: UpdateChildRequest): Observable<Child> {
    return this.http.put<Child>(`${this.base}/${id}`, data);
  }

  deleteChild(id: string): Observable<void> {
    return this.http.delete<void>(`${this.base}/${id}`);
  }

  adjustTokens(id: string, amount: number): Observable<Child> {
    return this.http.patch<Child>(`${this.base}/${id}/tokens`, { amount });
  }
}
