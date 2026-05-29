import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink } from '@angular/router';
import { TaskService } from '../../../core/services/task.service';
import { Task, TaskCategory, TASK_CATEGORY_LABELS } from '../../../core/models/task.model';
import { ChildNavComponent } from '../../../shared/components/child-nav/child-nav.component';
import { StarRatingComponent } from '../../../shared/components/star-rating/star-rating.component';

@Component({
  selector: 'app-task-board',
  standalone: true,
  imports: [CommonModule, RouterLink, ChildNavComponent, StarRatingComponent],
  templateUrl: './task-board.component.html',
  styleUrl: './task-board.component.scss'
})
export class TaskBoardComponent implements OnInit {
  tasks: Task[] = [];
  loading = true;
  error = '';

  readonly categoryLabels = TASK_CATEGORY_LABELS;

  constructor(private taskService: TaskService) {}

  ngOnInit(): void {
    this.taskService.getTasks().subscribe({
      next: tasks => {
        this.tasks = tasks;
        this.loading = false;
      },
      error: () => {
        this.error = 'Failed to load tasks. Please try again.';
        this.loading = false;
      }
    });
  }

  get todoTasks(): Task[] {
    return this.tasks.filter(t => t.status === 'OPEN' || t.status === 'IN_PROGRESS');
  }

  get submittedTasks(): Task[] {
    return this.tasks.filter(t => t.status === 'DONE');
  }

  get completedTasks(): Task[] {
    return this.tasks.filter(t => t.status === 'REVIEWED');
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
