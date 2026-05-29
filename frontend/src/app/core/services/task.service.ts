import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, of } from 'rxjs';
import { switchMap } from 'rxjs/operators';
import { environment } from '../../../environments/environment';
import { Task, TaskAttachment, CreateTaskRequest } from '../models/task.model';

export interface SubmitTaskRequest {
  childNote: string;
  childSelfRating: number;
}

export interface ReviewTaskRequest {
  parentRating: number;
  parentComment?: string;
}

@Injectable({ providedIn: 'root' })
export class TaskService {
  private readonly base = `${environment.apiUrl}/tasks`;

  constructor(private http: HttpClient) {}

  getTasks(params?: { childId?: string; status?: string }): Observable<Task[]> {
    return this.http.get<Task[]>(this.base, { params: params as Record<string, string> });
  }

  getTask(id: string): Observable<Task> {
    return this.http.get<Task>(`${this.base}/${id}`);
  }

  createTask(data: CreateTaskRequest): Observable<Task[]> {
    return this.http.post<Task[]>(this.base, data);
  }

  startTask(id: string): Observable<Task> {
    return this.http.put<Task>(`${this.base}/${id}/start`, {});
  }

  submitTask(id: string, data: SubmitTaskRequest): Observable<Task> {
    return this.http.put<Task>(`${this.base}/${id}/submit`, data);
  }

  uploadAttachments(id: string, files: File[]): Observable<unknown> {
    const formData = new FormData();
    files.forEach(f => formData.append('files', f));
    return this.http.post(`${this.base}/${id}/attachments`, formData);
  }

  submitWithAttachments(id: string, data: SubmitTaskRequest, files: File[]): Observable<Task> {
    const upload$ = files.length > 0 ? this.uploadAttachments(id, files) : of(null);
    return upload$.pipe(switchMap(() => this.submitTask(id, data)));
  }

  reviewTask(id: string, data: ReviewTaskRequest): Observable<Task> {
    return this.http.put<Task>(`${this.base}/${id}/review`, data);
  }

  getTaskAttachments(id: string): Observable<TaskAttachment[]> {
    return this.http.get<TaskAttachment[]>(`${this.base}/${id}/attachments`);
  }

  downloadAttachment(taskId: string, attachmentId: string): Observable<Blob> {
    return this.http.get(`${this.base}/${taskId}/attachments/${attachmentId}/download`, {
      responseType: 'blob'
    });
  }
}
