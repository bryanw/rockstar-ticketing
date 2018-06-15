import { Injectable } from '@angular/core';
import { Router, CanActivate, ActivatedRouteSnapshot, RouterStateSnapshot } from '@angular/router';
import {Observable} from 'rxjs/Observable';

@Injectable()
export class AuthGuard implements CanActivate {

    constructor(private router: Router) { }

    canActivate(route: ActivatedRouteSnapshot, state: RouterStateSnapshot): Observable<boolean>|boolean {
        console.log('AuthGuard.canActivate() - 1');
        /*
        if (localStorage.getItem('currentUser')) {
            return true;
        }

        this.router.navigate(['/login'], { queryParams: { returnUrl: state.url }});
        return false;
        */
        const page = route.queryParams['page'];
        console.log(`page = ${page}`);
        if (page && page === 'options') {
            console.log('Navigate to /options');
            this.router.navigate(['/options']);
            return false;
        }

        return true;
    }
}
