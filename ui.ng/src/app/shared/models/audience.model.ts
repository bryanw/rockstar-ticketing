export class Audience {

    constructor(public id: number = 0,
      name: string = "",
      description: string = "",
      origin: string = "",
      modifiedAt: string = "",
      targetRule?: TargetRule) {

    }
}

export class TargetRule {
}

export class TargetRuleGroup extends TargetRule {
  isAnd: true;
  rules: Array<TargetRule> = [];
}

export class VisitorProfileAttributeRule extends TargetRule {
  crs: '';
  operator: 'equalsIgnoreCase';
  values: Array<string> = [];
}

/*
"id": 1673726,
  "name": "Bangarang Golf Enthusi-1503612183791",
  "description": "--",
  "origin": "target",
  "targetRule": {
  "crs": "BangarangMSDynamics.Primary Interest",
    "equalsIgnoreCase": [
    "Golf"
  ]
},
"modifiedAt": "2017-08-24T22:03:06Z"
*/
