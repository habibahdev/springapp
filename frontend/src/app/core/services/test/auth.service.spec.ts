import {TestBed} from '@angular/core/testing';
import {HttpTestingController, provideHttpClientTesting} from '@angular/common/http/testing';
import {provideHttpClient} from '@angular/common/http';
import {provideRouter, Router} from '@angular/router';
import {afterEach, beforeEach, describe, expect, it, vi} from 'vitest';

import {AuthService} from '../auth.service';
import {environment} from '../../../../environments/environment';
import {AuthResponse, LoginRequest, RegisterRequest} from '../../models/auth.model';

describe('AuthService', () => {
  let service: AuthService;
  let httpTestingController: HttpTestingController;
  let router: Router;

  const apiUrl = `${environment.apiUrl}/auth`;
  const storageKey = 'springapp_session';

  /**
   * Génère un faux JWT contenant une date d'expiration.
   */
  const createToken = (
    exp: number = Math.floor(Date.now() / 1000) + 3600
  ): string => {
    const payload = btoa(JSON.stringify({exp}));
    return `header.${payload}.signature`;
  };

  const mockUserSession: AuthResponse = {
    token: createToken(),
    fullName: 'Jean Dupont',
    email: 'jean.dupont@example.com',
    role: 'USER'
  };

  const mockAdminSession: AuthResponse = {
    token: createToken(),
    fullName: 'Admin SpringApp',
    email: 'admin@springapp.test',
    role: 'ADMIN'
  };

  const mockLoginRequest: LoginRequest = {
    email: 'jean.dupont@example.com',
    password: 'password123'
  };

  const mockRegisterRequest: RegisterRequest = {
    fullName: 'Jean Dupont',
    email: 'jean.dupont@example.com',
    password: 'password123'
  };

  beforeEach(() => {
    localStorage.clear();

    TestBed.configureTestingModule({
      providers: [
        AuthService,
        provideHttpClient(),
        provideHttpClientTesting(),
        provideRouter([])
      ]
    });

    service = TestBed.inject(AuthService);
    httpTestingController = TestBed.inject(HttpTestingController);
    router = TestBed.inject(Router);
  });

  afterEach(() => {
    httpTestingController.verify();
  });


  describe('initial session', () => {
    it('should initialize with no authenticated user', () => {
      expect(service.session()).toBeNull();
      expect(service.isLoggedIn()).toBe(false);
      expect(service.isAdmin()).toBe(false);
      expect(service.fullName()).toBe('');
      expect(service.token).toBeNull();
    });

    it('should restore a valid session from localStorage', () => {
      localStorage.setItem(storageKey, JSON.stringify(mockUserSession));

      // Crée une nouvelle instance pour relire le localStorage.
      const authService = TestBed.runInInjectionContext(
        () => new AuthService()
      );

      expect(authService.session()).toEqual(mockUserSession);
      expect(authService.isLoggedIn()).toBe(true);
      expect(authService.isAdmin()).toBe(false);
      expect(authService.fullName()).toBe('Jean Dupont');
      expect(authService.token).toBe(mockUserSession.token);
    });

    it('should remove an expired session from localStorage', () => {
      const expiredSession: AuthResponse = {
        ...mockUserSession,
        token: createToken(Math.floor(Date.now() / 1000) - 3600)
      };

      localStorage.setItem(storageKey, JSON.stringify(expiredSession));

      const authService = TestBed.runInInjectionContext(
        () => new AuthService()
      );

      expect(authService.session()).toBeNull();
      expect(authService.isLoggedIn()).toBe(false);
      expect(localStorage.getItem(storageKey)).toBeNull();
    });

    it('should initialize without a session when the stored token is invalid', () => {
      const invalidSession = {
        ...mockUserSession,
        token: 'invalid-token'
      };

      localStorage.setItem(storageKey, JSON.stringify(invalidSession));

      const authService = TestBed.runInInjectionContext(
        () => new AuthService()
      );

      expect(authService.session()).toBeNull();
      expect(authService.isLoggedIn()).toBe(false);
    });
  });


  describe('login', () => {
    it('should authenticate a user and save the session', () => {
      service.login(mockLoginRequest).subscribe((response) => {
        expect(response).toEqual(mockUserSession);
      });

      const request = httpTestingController.expectOne(`${apiUrl}/login`);

      expect(request.request.method).toBe('POST');
      expect(request.request.body).toEqual(mockLoginRequest);

      request.flush(mockUserSession);

      expect(service.session()).toEqual(mockUserSession);
      expect(service.isLoggedIn()).toBe(true);
      expect(service.isAdmin()).toBe(false);
      expect(service.fullName()).toBe('Jean Dupont');
      expect(service.token).toBe(mockUserSession.token);

      expect(localStorage.getItem(storageKey)).toBe(
        JSON.stringify(mockUserSession)
      );
    });
  });


  describe('register', () => {
    it('should register a user and save the session', () => {
      service.register(mockRegisterRequest).subscribe((response) => {
        expect(response).toEqual(mockUserSession);
      });

      const request = httpTestingController.expectOne(`${apiUrl}/register`);

      expect(request.request.method).toBe('POST');
      expect(request.request.body).toEqual(mockRegisterRequest);

      request.flush(mockUserSession);

      expect(service.session()).toEqual(mockUserSession);
      expect(service.isLoggedIn()).toBe(true);
      expect(localStorage.getItem(storageKey)).toBe(
        JSON.stringify(mockUserSession)
      );
    });
  });


  describe('logout', () => {
    it('should clear the session and redirect to login', () => {
      localStorage.setItem(storageKey, JSON.stringify(mockUserSession));

      // Recrée le service avec une session déjà enregistrée.
      const authService = TestBed.runInInjectionContext(
        () => new AuthService()
      );

      const navigateSpy = vi.spyOn(router, 'navigate')
        .mockResolvedValue(true);

      authService.logout();

      expect(authService.session()).toBeNull();
      expect(authService.isLoggedIn()).toBe(false);
      expect(authService.isAdmin()).toBe(false);
      expect(authService.fullName()).toBe('');
      expect(authService.token).toBeNull();

      expect(localStorage.getItem(storageKey)).toBeNull();
      expect(navigateSpy).toHaveBeenCalledWith(['/login']);
    });
  });


  describe('roles', () => {
    it('should identify an administrator', () => {
      localStorage.setItem(storageKey, JSON.stringify(mockAdminSession));

      const authService = TestBed.runInInjectionContext(
        () => new AuthService()
      );

      expect(authService.isLoggedIn()).toBe(true);
      expect(authService.isAdmin()).toBe(true);
      expect(authService.fullName()).toBe('Admin SpringApp');
    });

    it('should not identify a regular user as an administrator', () => {
      localStorage.setItem(storageKey, JSON.stringify(mockUserSession));

      const authService = TestBed.runInInjectionContext(
        () => new AuthService()
      );

      expect(authService.isLoggedIn()).toBe(true);
      expect(authService.isAdmin()).toBe(false);
    });
  });
});
