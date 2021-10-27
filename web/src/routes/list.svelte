<script>
    import {gateway, title, variables} from "$lib/variables";
    import {fly, scale, slide} from "svelte/transition";
    import {elasticInOut, quintOut} from "svelte/easing";
    import {onDestroy, onMount} from "svelte";

    title.set("Shopping List");

    const custom = (node, {
        delay = 0,
        duration = 400,
        easing: easing$1 = elasticInOut,
        start = 0,
        opacity = 0,
        enabled = true
    } = {}) => {
        if (!enabled) return;

        const style = getComputedStyle(node);
        const target_opacity = +style.opacity;
        const transform = style.transform === 'none' ? '' : style.transform;
        const sd = 1 - start;
        const od = target_opacity * (1 - opacity);
        return {
            delay,
            duration,
            easing: easing$1,
            css: (_t, u) => `
			position: absolute;
			transform: ${transform} scale(${1 - (sd * u)});
			opacity: ${target_opacity - (od * u)}
		`
        };
    };


    let items = [
        {
            text: "",
            checked: false,
            uid: {}
        }
    ];

    let complete = 0;

    $: {
        complete = 0;
        items.forEach(item => {
            if (item.checked)
                complete++;
        });

        if (items.length > 0 && items[items.length - 1].text) {
            items.push({text: "", checked: false, uid: {}});
        }

        if (items.length > 1 && !items[items.length - 2].text) {
            items.pop();
            items[items.length - 1].checked = false;
        }
    }

    const getList = () => {
        fetch(gateway() + "/auth-service/list")
            .then(res => res.json())
            .then(data => {
                console.log(data);
                items = data;
                items.forEach(itm => itm.uid = {});
            })
            .catch(e => {
                console.error(e);
            });
    };

    const saveList = () => {
        let tmp = [...items];
        tmp.pop();
        fetch(gateway() + "/auth-service/list", {
            method: "post",
            headers: {'Content-Type': 'application/json'},
            body: JSON.stringify(tmp)
        })
            .then(res => res.json())
            .then(data => {
                if (!data?.success) {
                    alert("Shopping list could not be saved!");
                }
            })
            .catch(err => {
                console.error(err);
            });
    };

    let unsubscribe = variables.jwt.subscribe(getList);
    onMount(getList);
    onDestroy(unsubscribe);

    let itmCount = 0;
    $: itmCount = items.length - 1;

    const checkNext = (e, index) => {
        if (e.keyCode == 13) {
            let next = document.getElementById("input" + (index + 1));
            if (next) next.focus();
        }
    };

    const checkPrev = (e, index) => {
        if (e.keyCode == 8 && e.target.value == "") {
            let prev = document.getElementById("input" + (index - 1));
            if (prev) prev.focus();
        }
    };

    const check = index => {
        let itm = items[index];
        itm.checked = !itm.checked;
        items = [...items];
        saveList();
    };

</script>

<div class="container" out:slide>
    <div class="header">
        <div class="count">
            {#key itmCount}
                <span style="display: inline-block" in:fly={{y: -10}}>{itmCount}</span>
            {/key}
            items.
        </div>
        <div class="complete">
            (
            {#key complete}<span style="display: block" in:fly={{y: -10}}>{complete}</span>{/key}/
            {#key itmCount}<span style="display: block" in:fly={{y: -10}}>{itmCount}</span>{/key}
            &nbsp;complete)
        </div>
    </div>
    <div class="items">
        {#each items as itm, i (itm.uid)}
            <div class="row" transition:slide>
                {#if itm.text}
                    {#if itm.checked}
                        <div class="material-icons-round check"
                             on:click={() => check(i)}
                             out:custom={{duration: 500}}
                             in:scale={{duration: 750, easing: elasticInOut}}>
                            check_box
                        </div>
                    {:else}
                        <div class="material-icons-round check"
                             on:click={() => check(i)}
                             out:custom={{duration: 500}}
                             in:scale={{duration: 750, easing: elasticInOut}}>
                            check_box_outline_blank
                        </div>
                    {/if}
                {:else}
                    <div class="material-icons-round add"
                         in:scale={{duration: 750, easing: elasticInOut}}
                         out:custom={{enabled: itm.text, duration: 500, easing: quintOut}}>add
                    </div>
                {/if}

                <input id="input{i}" type="text" bind:value="{itm.text}" placeholder="New item..."
                       on:keypress={e => checkNext(e, i)}
                       on:keydown={e => checkPrev(e, i)}/>
            </div>
        {/each}
    </div>

    <div class="button" on:click={saveList}>Save List</div>
</div>

<style>
    .container {
        position: relative;
        display: flex;
        flex-direction: column;
        align-items: stretch;
        justify-content: center;
        width: 30%;
        min-width: 200px;
        margin: 0 auto;
        flex-grow: 1;
    }

    .row {
        position: relative;
        padding: 0.5em;
        border-bottom: 1px solid;
        border-image-source: linear-gradient(to right, rgba(0, 0, 0, 0), var(--disabled-fg-color),
        rgba(0, 0, 0, 0), rgba(0, 0, 0, 0));
        border-image-slice: 1;
        display: flex;
        align-items: center;
        transition: border 1s linear;
    }

    .items > div:last-child {
        /*border-bottom: 1px solid;*/
        border-bottom: none;
    }

    .add, .check {
        margin-right: 0.5em;
        user-select: none;
    }

    .check:hover {
        cursor: pointer;
    }

    input {
        width: 100%;
        border: none;
        border-left: 1px solid black;
        padding: 0 0 0 0.5em;
        background: transparent;
        margin: 0;
        font-size: 1em;
        position: relative;
    }

    input:focus {
        outline: none;
    }

    .complete, .header {
        display: flex;
    }

    .count {
        flex-grow: 1;
    }

    .complete {
        margin-left: 0.5em;
        color: var(--disabled-fg-color)
    }
</style>