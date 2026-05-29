import { Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { TaskService } from '../../../core/services/task.service';
import { Task, TaskCategory, TASK_CATEGORY_LABELS } from '../../../core/models/task.model';
import { ChildNavComponent } from '../../../shared/components/child-nav/child-nav.component';
import { StarRatingComponent } from '../../../shared/components/star-rating/star-rating.component';

const ACCEPTED_TYPES = '.jpg,.jpeg,.png,.gif,.pdf,.doc,.docx,.ppt,.pptx,.mp4,.mov';

@Component({
  selector: 'app-task-detail',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterLink, ChildNavComponent, StarRatingComponent],
  templateUrl: './task-detail.component.html',
  styleUrl: './task-detail.component.scss'
})
export class TaskDetailComponent implements OnInit {
  private taskId = inject(ActivatedRoute).snapshot.paramMap.get('id')!;

  task: Task | null = null;
  loading = true;
  error = '';

  starting = false;
  startError = '';

  childNote = '';
  selfRating = 0;
  selectedFiles: File[] = [];
  submitting = false;
  submitError = '';
  submitSuccess = false;

  readonly categoryLabels = TASK_CATEGORY_LABELS;
  readonly acceptedTypes = ACCEPTED_TYPES;

  constructor(private taskService: TaskService) {}

  ngOnInit(): void {
    this.loadTask();
  }

  private loadTask(): void {
    this.loading = true;
    this.taskService.getTask(this.taskId).subscribe({
      next: task => {
        this.task = task;
        this.loading = false;
      },
      error: () => {
        this.error = 'Task not found.';
        this.loading = false;
      }
    });
  }

  startTask(): void {
    this.starting = true;
    this.startError = '';
    this.taskService.startTask(this.taskId).subscribe({
      next: task => {
        this.task = task;
        this.starting = false;
      },
      error: () => {
        this.startError = 'Failed to start task. Please try again.';
        this.starting = false;
      }
    });
  }

  onFilesSelected(event: Event): void {
    const input = event.target as HTMLInputElement;
    this.selectedFiles = input.files ? Array.from(input.files) : [];
  }

  removeFile(index: number): void {
    this.selectedFiles = this.selectedFiles.filter((_, i) => i !== index);
  }

  submitTask(): void {
    this.submitError = '';
    if (!this.childNote.trim()) {
      this.submitError = 'Please describe what you did.';
      return;
    }
    if (!this.selfRating) {
      this.submitError = 'Please select a self-rating.';
      return;
    }

    this.submitting = true;
    this.taskService.submitWithAttachments(
      this.taskId,
      { childNote: this.childNote.trim(), childSelfRating: this.selfRating },
      this.selectedFiles
    ).subscribe({
      next: task => {
        this.task = task;
        this.submitSuccess = true;
        this.submitting = false;
      },
      error: () => {
        this.submitError = 'Submission failed. Please try again.';
        this.submitting = false;
      }
    });
  }

  statusLabel(status: string): string {
    switch (status) {
      case 'OPEN': return 'Open';
      case 'IN_PROGRESS': return 'In Progress';
      case 'DONE': return 'Submitted';
      case 'REVIEWED': return 'Reviewed';
      default: return status;
    }
  }

  statusBadgeClass(status: string): string {
    const map: Record<string, string> = {
      OPEN: 'fq-badge-open', IN_PROGRESS: 'fq-badge-in-progress',
      DONE: 'fq-badge-done', REVIEWED: 'fq-badge-reviewed'
    };
    return map[status] ?? 'fq-badge-open';
  }

  categoryBadgeClass(category: TaskCategory): string {
    const map: Record<TaskCategory, string> = {
      SPORT_AND_ACTIVITY: 'fq-category-sport',
      STUDY_AND_LEARNING: 'fq-category-study',
      CHORES: 'fq-category-chores',
      HEALTH_AND_HYGIENE: 'fq-category-health',
      CREATIVE: 'fq-category-creative',
      SOCIAL_AND_FAMILY: 'fq-category-social',
      DIGITAL_WELLBEING: 'fq-category-digital'
    };
    return map[category] ?? 'fq-category-study';
  }
}
