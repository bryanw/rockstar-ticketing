export class Options {
    constructor(public imsHost: string = '',
                public clientId: string = '',
                public clientSecret: string = '',
                public orgId: string = '',
                public technicalAccountId: string = '',
                public secretKey: string = '',
                public jwtToken: string = '',
                public bearerToken: string = '',
                public youtubeApiKey: string = '') {
    }
}
