import { Component, OnDestroy } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink, RouterLinkActive } from '@angular/router';
import { Subscription } from 'rxjs';
import { AuthService } from '../../../core/services/auth.service';
import { TokenStorageService } from '../../../core/services/token-storage.service';

@Component({
  selector: 'app-child-nav',
  standalone: true,
  imports: [CommonModule, RouterLink, RouterLinkActive],
  templateUrl: './child-nav.component.html',
  styleUrl: './child-nav.component.scss'
})
export class ChildNavComponent implements OnDestroy {
  childName = '';
  childTokens = 0;
  private sub: Subscription;

  constructor(private auth: AuthService, private tokenStorage: TokenStorageService) {
    this.sub = this.tokenStorage.user$.subscribe(user => {
      this.childName = user?.fullName ?? '';
      this.childTokens = user?.tokens ?? 0;
    });
  }

  ngOnDestroy(): void {
    this.sub.unsubscribe();
  }

  logout(): void {
    this.auth.logout();
  }
}
