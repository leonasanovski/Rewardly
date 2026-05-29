import { Routes } from '@angular/router';
import { authGuard, roleGuard } from './core/guards/role.guard';

export const routes: Routes = [
  { path: '', redirectTo: 'auth/login', pathMatch: 'full' },

  {
    path: 'auth',
    children: [
      {
        path: 'login',
        loadComponent: () => import('./features/auth/login/login.component').then(m => m.LoginComponent)
      },
      {
        path: 'register',
        loadComponent: () => import('./features/auth/register/register.component').then(m => m.RegisterComponent)
      }
    ]
  },

  {
    path: 'parent',
    canActivate: [roleGuard('PARENT')],
    children: [
      { path: '', redirectTo: 'tasks', pathMatch: 'full' },
      {
        path: 'tasks',
        loadComponent: () => import('./features/parent/task-management/task-management.component').then(m => m.TaskManagementComponent)
      },
      {
        path: 'tasks/:id/review',
        loadComponent: () => import('./features/parent/task-review/task-review.component').then(m => m.TaskReviewComponent)
      },
      {
        path: 'rewards',
        loadComponent: () => import('./features/parent/reward-management/reward-management.component').then(m => m.RewardManagementComponent)
      },
      {
        path: 'children',
        loadComponent: () => import('./features/parent/children-management/children-management.component').then(m => m.ChildrenManagementComponent)
      }
    ]
  },

  {
    path: 'child',
    canActivate: [roleGuard('CHILD')],
    children: [
      { path: '', redirectTo: 'tasks', pathMatch: 'full' },
      {
        path: 'tasks',
        loadComponent: () => import('./features/child/task-board/task-board.component').then(m => m.TaskBoardComponent)
      },
      {
        path: 'tasks/:id',
        loadComponent: () => import('./features/child/task-detail/task-detail.component').then(m => m.TaskDetailComponent)
      },
      {
        path: 'rewards',
        loadComponent: () => import('./features/child/reward-market/reward-market.component').then(m => m.RewardMarketComponent)
      },
      {
        path: 'wallet',
        loadComponent: () => import('./features/child/wallet/wallet.component').then(m => m.WalletComponent)
      }
    ]
  },

  { path: '**', redirectTo: 'auth/login' }
];
