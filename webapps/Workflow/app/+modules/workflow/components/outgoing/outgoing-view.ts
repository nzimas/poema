import { Component, Input, OnInit, OnDestroy } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { Store } from '@ngrx/store';

import { EnvironmentActions } from '../../../../actions';
import { WorkflowOutgoingService } from '../../services';

@Component({
    selector: 'outgoing-view',
    template: `
        <list-page
            [title]="title"
            [selectable]="true"
            [headerVisible]="true"
            [titleVisible]="true"
            [actionsVisible]="true"
            [captionsVisible]="true"
            [activeSort]="activeSort"
            [list]="list"
            [meta]="meta"
            [actions]="actions"
            [columns]="columns"
            (openModel)="onOpenModel($event)"
            (action)="onAction($event)"
            (refresh)="refresh($event)"
            (sort)="onSort($event)"
            (goToPage)="goToPage($event)">
        </list-page>
    `,
    host: {
        '[class.loadable]': 'true',
        '[class.load]': 'loading'
    }
})

export class OutgoingViewComponent {
    @Input() embedded: boolean = false;
    @Input() selectable: boolean = true;
    @Input() headerVisible: boolean = true;
    @Input() titleVisible: boolean = true;
    @Input() actionsVisible: boolean = true;
    @Input() captionsVisible: boolean = true;

    private actions = [];
    private columns = [];
    private subs: any = [];

    title = 'outgoings';
    list: any[];
    meta: any = {};
    keyWord: string = '';
    loading: boolean = true;
    activeSort: string = '';
    private params: any = {};

    constructor(
        private store: Store<any>,
        private route: ActivatedRoute,
        private router: Router,
        private environmentActions: EnvironmentActions,
        private outgoingService: WorkflowOutgoingService
    ) { }

    ngOnInit() {
        this.subs.push(this.route.params.subscribe(params => {
            this.loadData(Object.assign({}, params, {
                page: this.meta.page
            }));
        }));
    }

    ngOnDestroy() {
        this.subs.map(s => s.unsubscribe());
    }

    loadData(params) {
        this.loading = true;
        this.params = Object.assign({}, params, {
            sort: this.activeSort || 'regDate:desc'
        });
        let typeId = 'outgoing';

        this.outgoingService.fetchOutgoings(this.params).subscribe(
            payload => {
                this.title = payload.payload.title;
                this.actions = payload.payload._actionbar.actions;
                this.columns = payload.payload._columnoptions.columns;
                let view = payload.payload.viewpage;
                this.list = view.result;
                this.meta = {
                    count: view.count,
                    keyWord: view.keyWord,
                    page: view.pageNum,
                    totalPages: view.maxPage
                };
                this.loading = false;
            },
            error => console.log(error)
        );
    }

    refresh() {
        this.loadData(this.params);
    }

    goToPage(params) {
        this.loadData(params);
    }

    onSort($event) {
        this.activeSort = $event;
        this.refresh();
    }

    onAction($event) {
        // this.incomingService.doIncomingAction('12312321', $event.action.customID).subscribe(payload => {
        //     let resp = payload.objects[0];
        //     alert(resp.name + ' : ' + resp.value);
        // });
        if ($event.action.url) {
            this.router.navigate([$event.action.url], { relativeTo: this.route });
        } else {
            console.log($event);
        }
    }

    onOpenModel(model) {
        console.log(model);
        this.router.navigate([model.id], { relativeTo: this.route });
    }
}
