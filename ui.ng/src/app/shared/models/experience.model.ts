import {OfferLocation} from './offer-location.model';
import {Audience} from "./audience.model";
import {Offer} from "./offer.model";

export class Experience {
    constructor(public experienceLocalId: string,
                public name: string = "",
                public audienceIds: number[] = [],
                public audience: Audience = new Audience(),
                public offerDetails: Offer = new Offer(),
                public offerLocations: OfferLocation[] = []) {

    }
}
