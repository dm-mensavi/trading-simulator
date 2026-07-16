import { Component, ChangeDetectorRef } from '@angular/core';
import { RouterOutlet, Router, NavigationEnd } from '@angular/router';
import { SidebarComponent } from './components/sidebar/sidebar';
import { CommonModule, AsyncPipe } from '@angular/common';
import { Observable } from 'rxjs';
import { filter, map, startWith } from 'rxjs/operators';

@Component({
  selector: 'app-root',
  standalone: true,
  imports: [RouterOutlet, SidebarComponent, CommonModule, AsyncPipe],
  template: `
    <ng-container *ngIf="showShell$ | async; else authPage">
      <div class="app-shell">
        <app-sidebar></app-sidebar>
        <main class="main-content">
          <router-outlet></router-outlet>
        </main>
      </div>
    </ng-container>
    <ng-template #authPage>
      <router-outlet></router-outlet>
    </ng-template>
  `,
  styles: [`
    .app-shell { display: flex; min-height: 100vh; }
    .main-content {
      margin-left: 240px;
      flex: 1;
      padding: 40px 48px;
      min-height: 100vh;
      background: var(--bg-primary);
    }
  `]
})
export class App {
  showShell$: Observable<boolean>;

  constructor(private router: Router, private cdr: ChangeDetectorRef) {
    this.showShell$ = this.router.events.pipe(
      filter(e => e instanceof NavigationEnd),
      map((e: any) => !e.urlAfterRedirects.startsWith('/login') && !e.urlAfterRedirects.startsWith('/register')),
      startWith(!this.router.url.startsWith('/login') && !this.router.url.startsWith('/register'))
    );
  }
}
