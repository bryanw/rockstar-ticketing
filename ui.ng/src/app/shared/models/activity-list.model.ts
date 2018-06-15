import {Activity} from './activity.model';

export class ActivityList {

    constructor(public total: number = 0,
                public offset: number = 0,
                public activities: Activity[] = []) {

    }
}