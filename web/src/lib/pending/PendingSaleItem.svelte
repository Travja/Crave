<script>
    import {createEventDispatcher, onMount} from "svelte";
    import {gateway} from "$lib/variables";
    import {formatter} from "$lib/util";

    const dispatch = createEventDispatcher();

    export let approved = false,
        id,
        sales;

    let startDate,
        endDate;

    const dateOpts = {weekday: 'long', year: 'numeric', month: 'short', day: 'numeric'};

    onMount(() => {
        console.log(sales);
        startDate = new Date(sales[0]?.startDate);
        endDate = new Date(sales[0]?.endDate);
    });

    const approve = () => {
        fetch(`${gateway()}/sale-service/sale/approve/${id}`, {method: "POST"})
            .then(res => {
                console.log(res);
                if (res.status == 204) {
                    dispatch('approved')
                }
            })
            .catch(e => {
                console.error(e);
            });
    };

    const reject = () => {
        fetch(`${gateway()}/sale-service/sale/reject/${id}`, {method: "POST"})
            .then(res => {
                console.log(res);
                if (res.status == 204) {
                    dispatch('rejected')
                }
            })
            .catch(e => {
                console.error(e);
            });
    };

</script>

{#if sales.length > 0}
    <div class="item">
        <div class="line-one">
            <span>
                <h4 class="wrapper">
                    {sales[0].store.name}
                    -
                    {sales[0].store.streetAddress}, {sales[0].store.city}
                </h4>
        <div class="dates">
            {startDate?.toLocaleDateString('en-us', dateOpts)} to {endDate?.toLocaleDateString('en-us', dateOpts)}
        </div>
            </span>
            <div class="filler"/>
            <div class="button approve"
                 on:click={approve}>
                <span class="material-icons-round">thumb_up</span>
                Approve
            </div>
            <div class="button reject"
                 on:click={reject}>
                <span class="material-icons-round">thumb_down</span>
                Reject
            </div>
        </div>
        {#each sales as sale}
            <div class="sale">
                <div>{sale.item.item.name}</div>
                <div class="priceWrap">
                    <div class="price">{formatter.format(sale.item.price)}</div>
                    <div class="material-icons-round">east</div>
                    <div class="price">{formatter.format(sale.newPrice)}</div>
                </div>
            </div>
        {/each}
    </div>
{/if}

<style>
    .button {
        margin: 0 0 0 0.5rem;
        display: flex;
        flex-direction: row;
        align-items: center;
    }

    .button .material-icons-round {
        margin-right: 0.3em;
    }

    h4 {
        margin: 0;
    }

    .line-one {
        display: flex;
        align-items: center;
    }

    .item {
        position: relative;
        width: 100%;
        padding: 0.5rem;
        box-sizing: border-box;
        border-left: 0.2rem solid var(--accent-color);
        margin-bottom: 1rem;
    }

    .item:hover {
        background: #eee;
    }

    .item:nth-child(2n-1) {
        background: var(--bg-color-secondary);
    }

    .wrapper {
        display: block;
    }

    .sale {
        padding-left: 0.5em;
        border-left: 2px solid var(--accent-color);
        margin: 0.5rem 0;
        margin-left: 0.5em;
    }

    .priceWrap {
        display: flex;
        align-items: center;
        font-size: 0.9em;
        padding: 0.2em 0;
    }

    .price {
        background: var(--accent-color);
        color: white;
        font-weight: bold;
        border-radius: 0.2em;
        padding: 0.4em;
        display: inline-block;
        box-sizing: border-box;
        background: var(--accent-color);
    }

    .filler {
        flex-grow: 1;
    }

    .approve {
        background: green;
        color: white;
    }

    .reject {
        background: red;
        color: white;
    }
</style>