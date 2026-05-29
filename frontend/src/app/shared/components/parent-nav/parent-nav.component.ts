import { Component } from '@angular/core';
import { RouterLink, RouterLinkActive } from '@angular/router';
import { AuthService } from '../../../core/services/auth.service';
import { TokenStorageService } from '../../../core/services/token-storage.service';

@Component({
  selector: 'app-parent-nav',
  standalone: true,
  imports: [RouterLink, RouterLinkActive],
  templateUrl: './parent-nav.component.html',
  styleUrl: './parent-nav.component.scss'
})
export class ParentNavComponent {
  constructor(
    private auth: AuthService,
    private tokenStorage: TokenStorageService
  ) {}

  get parentName(): string {
    return this.tokenStorage.getUser()?.fullName ?? '';
  }

  logout(): void {
    this.auth.logout();
  }
}
