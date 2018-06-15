import {Offer} from "./offer.model";

export class OfferList {

    constructor(public offset: number = 0,
                public limit: number = 0,
                public total: number = 0,
                public offers: Offer[] = []) {

    }
}