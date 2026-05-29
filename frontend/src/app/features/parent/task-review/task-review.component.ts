import { Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { forkJoin } from 'rxjs';
import { TaskService } from '../../../core/services/task.service';
import { Task, TaskAttachment, TaskCategory, TASK_CATEGORY_LABELS } from '../../../core/models/task.model';
import { ParentNavComponent } from '../../../shared/components/parent-nav/parent-nav.component';
import { StarRatingComponent } from '../../../shared/components/star-rating/star-rating.component';

@Component({
  selector: 'app-task-review',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterLink, ParentNavComponent, StarRatingComponent],
  templateUrl: './task-review.component.html',
  styleUrl: './task-review.component.scss'
})
export class TaskReviewComponent implements OnInit {
  private taskId = inject(ActivatedRoute).snapshot.paramMap.get('id')!;

  task: Task | null = null;
  attachments: TaskAttachment[] = [];
  loading = true;
  error = '';

  parentRating = 0;
  parentComment = '';
  submitting = false;
  submitError = '';
  successMessage = '';

  downloadingId: string | null = null;

  readonly categoryLabels = TASK_CATEGORY_LABELS;

  constructor(private taskService: TaskService, private router: Router) {}

  ngOnInit(): void {
    forkJoin({
      task: this.taskService.getTask(this.taskId),
      attachments: this.taskService.getTaskAttachments(this.taskId)
    }).subscribe({
      next: ({ task, attachments }) => {
        this.task = task;
        this.attachments = attachments;
        this.loading = false;
      },
      error: () => {
        this.error = 'Failed to load task.';
        this.loading = false;
      }
    });
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

  downloadAttachment(attachment: TaskAttachment): void {
    this.downloadingId = attachment.id;
    this.taskService.downloadAttachment(this.taskId, attachment.id).subscribe({
      next: blob => {
        const url = URL.createObjectURL(blob);
        const a = document.createElement('a');
        a.href = url;
        a.download = attachment.fileName;
        a.click();
        URL.revokeObjectURL(url);
        this.downloadingId = null;
      },
      error: () => { this.downloadingId = null; }
    });
  }

  get tokenPreview(): number {
    if (!this.task || !this.parentRating) return 0;
    return Math.round((this.parentRating / 5) * this.task.tokenValue);
  }

  submitReview(): void {
    if (!this.parentRating) {
      this.submitError = 'Please select a rating.';
      return;
    }
    this.submitting = true;
    this.submitError = '';

    this.taskService.reviewTask(this.taskId, {
      parentRating: this.parentRating,
      parentComment: this.parentComment.trim() || undefined
    }).subscribe({
      next: task => {
        this.successMessage = `Review submitted! ${task.tokensEarned} tokens awarded to ${task.assignedTo.fullName}.`;
        this.submitting = false;
        setTimeout(() => this.router.navigate(['/parent/tasks']), 2000);
      },
      error: err => {
        this.submitError = err.error?.error || 'Failed to submit review.';
        this.submitting = false;
      }
    });
  }
}
