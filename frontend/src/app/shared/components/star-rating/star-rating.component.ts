import { Component, Input, Output, EventEmitter } from '@angular/core';
import { CommonModule } from '@angular/common';

const RATING_LABELS: Record<number, string> = {
  1: 'I barely did it',
  2: 'I did it but not my best',
  3: 'I did it okay',
  4: 'I did it well',
  5: 'I did my absolute best'
};

@Component({
  selector: 'app-star-rating',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './star-rating.component.html',
  styleUrl: './star-rating.component.scss'
})
export class StarRatingComponent {
  @Input() rating = 0;
  @Input() readonly = false;
  @Output() ratingChange = new EventEmitter<number>();

  readonly stars = [1, 2, 3, 4, 5];
  readonly labels = RATING_LABELS;
  hovered = 0;

  setRating(value: number): void {
    if (this.readonly) return;
    this.rating = value;
    this.ratingChange.emit(value);
  }

  setHovered(value: number): void {
    if (!this.readonly) this.hovered = value;
  }

  clearHover(): void {
    this.hovered = 0;
  }

  isActive(star: number): boolean {
    return star <= (this.hovered || this.rating);
  }
}
