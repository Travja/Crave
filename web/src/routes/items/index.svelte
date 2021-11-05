<!--<script context="module">-->
<!--    export const prerender = true;-->
<!--</script>-->
<script>
    import {gateway, title, variables} from "$lib/variables";
    import {onDestroy, onMount} from "svelte";
    import PageItem from "$lib/PageItem.svelte";
    import {fly} from 'svelte/transition';
    import SearchBar from "$lib/ui/SearchBar.svelte";

    title.set("Items");

    let tip = true;
    let gate;
    let items, list = [], fullList = [], error;
    let inProgress = false;

    const getLocation = (callback) => {
        if (navigator.geolocation) {
            navigator.geolocation.getCurrentPosition(pos => callback(pos));
        } else {
            alert("Geolocation is not supported by this browser.");
        }
    };

    const loadShoppingList = async () => {
        fetch(gateway() + "/auth-service/list")
            .then(res => res.json())
            .then(data => {
                list = [];
                for (let e in data) {
                    list.push(data[e].text);
                    fullList.push(data[e]);
                }
            })
            .catch(e => console.error(e));
    }

    const getItems = async (query, storeFilter, distanceFilter) => {
        if (!gate)
            return;

        error = undefined;
        inProgress = true;
        const get = async (queries) => {
            let q = "";
            console.log(queries);
            for (let qu in queries) {
                let param = queries[qu];
                if (qu == "store" && param == "all")
                    continue;
                q += (q.length > 1 ? "&" : "?") + qu + "=" + param;
                console.log(qu + ": " + param);
            }
            let url = gate + '/item-service/items' + q;
            console.log("Fetching items: " + url);

            const res = await fetch(url);
            inProgress = false;
            if (res.ok) {
                items = await res.json();
                console.log("Items: ", items);
                return;
            }

            error = await res.json();
            // console.error("Could not fetch items.");
            // console.error(error);
            switch (error.status) {
                case 401:
                    error = "User not authenticated.";
                    break;
                case 403:
                    error = "User is not allowed to access this resource";
                    break;
                case 503:
                    error = "Services unavailable";
                    break;
                default:
                    error = "An unknown error occurred.";
            }
        };

        let queries = {};
        if (query)
            queries.query = query;
        if (storeFilter)
            queries.store = storeFilter;
        if (distanceFilter && distanceFilter > 0) {
            queries.distance = distanceFilter;
            getLocation(pos => {
                console.log(pos);
                queries.lat = pos.coords.latitude;
                queries.lon = pos.coords.longitude;
                get(queries);
            });
        } else {
            await get(queries);
        }
    };

    onMount(async () => {
        // gateway = variables.gateway ? variables.gateway : window.location.origin;
        gate = gateway();

        await loadShoppingList();
        await getItems();
    });

    let unsubscribe = variables.jwt.subscribe(() => getItems());
    onDestroy(unsubscribe);

    const search = (e) => {
        let terms = e.detail.search;
        let storeFilter = e.detail.filters.stores;
        let distanceFilter = e.detail.filters.distance;
        getItems(terms, storeFilter, distanceFilter);
    };

    const addToList = item => {
        console.log(item);
        list.push(item);
        fullList.push({id: fullList.length + 1, text: item, checked: false});
        let tmp = [...fullList];

        let map = {};
        for (let i = 0; i < tmp.length; i++) {
            map[i] = tmp[i];
        }
        fetch(gateway() + "/auth-service/list", {
            method: "post",
            headers: {'Content-Type': 'application/json'},
            body: JSON.stringify(map)
        })
            .then(res => res.json())
            .then(data => {
                if (!data?.success) {
                    alert("Shopping list could not be saved!");
                } else {
                    list = [...list];
                }
            })
            .catch(err => {
                console.error(err);
            });
    };
</script>
<section>
    <h1>Directory</h1>
    <div id="intro">
        <p>Finding the lowest prices has never been so easy!</p>
    </div>
    <div class="divider">
        <hr>
        <div class="sp"/>
        <SearchBar on:search={search}/>
    </div>
    {#if items}
        <div id="items">
            {#each items as item, i}
                {#if item.details}
                    <PageItem {...item} inShoppingList="{list.includes(item.name)}"
                              on:addtolist={() => addToList(item.name)}/>
                    <!--{#if i < items.length - 1}-->
                    <!--    <div class="spacer"/>-->
                    <!--{/if}-->
                {/if}
            {/each}
            {#each Array(8) as i}
                <div class="fix"/>
            {/each}
        </div>
    {:else if inProgress}
        <p class="no-items">Fetching items..</p>
    {:else if error}
        <p class="no-items">Could not fetch items. {error}</p>
    {:else}
        <p class="no-items">No items available.</p>
    {/if}
</section>
{#if tip}
    <div id="tip"
         on:click={(e) => tip = false}
         out:fly="{{ x: 200, duration: tip == false ? 250 : 0}}">
        <div>Can't find what you're looking for? Make sure to add it!</div>
        <a alt="Scan a Receipt!" class="button" href="/scan" id="scan">Scan a Receipt!</a>
    </div>
{/if}
<style>
    section {
        display: flex;
        flex-direction: column;
        /*justify-content: center;*/
        /*align-items: center;*/
        flex: 1;
    }

    h1 {
        font-size: 2rem;
        margin: 0;
    }

    #intro {
        display: flex;
        align-items: center;
    }

    #items {
        position: relative;
        display: flex;
        flex-wrap: wrap;
        background: var(--bg-color);
    }

    .fix {
        display: flex;
        flex: auto;
        background: transparent;
        padding: 0px 21px;
        flex: 1;
    }

    .fix::after {
        content: "";
        min-width: 200px;
        max-width: 200px;
    }

    .no-items {
        color: #666;
        margin-left: 10px;
    }

    hr {
        width: 30%;
        margin: 0.5em 0;
    }

    #tip {
        position: fixed;
        bottom: 1em;
        right: 1em;
        background: #ddd;
        border-radius: 1em;
        padding: 1em;
        display: flex;
        flex-direction: column;
        align-items: flex-end;
        box-shadow: 0 0 15px #333;
        z-index: 1000;
    }

    .divider {
        display: flex;
        align-items: center;
        margin: 5px 0;
    }

    hr {
        display: inline-block;
    }
</style>