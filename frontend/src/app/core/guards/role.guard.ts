import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';
import { TokenStorageService } from '../services/token-storage.service';

export function roleGuard(requiredRole: 'PARENT' | 'CHILD'): CanActivateFn {
  return () => {
    const tokenStorage = inject(TokenStorageService);
    const router = inject(Router);
    const user = tokenStorage.getUser();
    if (!user) {
      router.navigate(['/auth/login']);
      return false;
    }
    if (user.role !== requiredRole) {
      router.navigate([user.role === 'PARENT' ? '/parent/children' : '/child/tasks']);
      return false;
    }
    return true;
  };
}

export const authGuard: CanActivateFn = () => {
  const tokenStorage = inject(TokenStorageService);
  const router = inject(Router);
  if (!tokenStorage.getToken()) {
    router.navigate(['/auth/login']);
    return false;
  }
  return true;
};
