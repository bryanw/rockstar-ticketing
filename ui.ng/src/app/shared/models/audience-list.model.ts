import {Audience} from "./audience.model";

export class AudienceList {

    constructor(public offset: number = 0,
                public limit: number = 0,
                public total: number = 0,
                public audiences: Audience[] = []) {

    }
}