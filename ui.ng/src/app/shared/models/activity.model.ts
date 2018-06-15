import {Experience} from './experience.model';

export class Activity {

  constructor(public id: number = 0,
              public thirdPartyId: string = "",
              public name: string = "",
              public state: string = "approved",
              public priority: number = 10,
              public autoAllocateTraffic: AutoAllocateTraffic = new AutoAllocateTraffic(),
              public locations: Locations = new Locations(),
              public experiences: Experience[] = [],
              public metrics: Metric[] = [new Metric()]) {

  }
}

export class AutoAllocateTraffic {
    constructor(public enabled: boolean = false,
                public successEvaluationCriteria: string = 'conversion_rate') {

    }
}

export class Locations {
    constructor(public mboxes: Mbox[] = [new Mbox()]) {

    }
}

export class Mbox {
    constructor(public locationLocalId: number = 0,
                public name: string = 'rockstar-mbox-1') {

    }
}

export class Metric {
    constructor(public metricLocalId: number = 32767,
                public name: string = 'MY PRIMARY GOAL',
                public conversion: boolean = true,
                public engagement: string = 'page_count',
                public action: MetricAction = new MetricAction(),
                public mboxes: MetricMbox[] = [new MetricMbox()]) {

    }

}

export class MetricAction {
    constructor(public type: string = 'count_once') {

    }

}

export class MetricMbox  {
    constructor(public name: string = 'rockstar-mbox-1',
                public successEvent: string = 'mbox_shown') {

    }

}


