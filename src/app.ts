// managon — English-language landing page for Minoru Law Office (みのる法律事務所).
// Built on commission for the firm. Data sourced from the firm's bengo4.com listing.

interface Env {
  APP_NANOID?: string;
  APP_VERSION?: string;
  APP_DEPLOY_AT?: string;
}

interface ExportedHandler<E> {
  fetch(req: Request, env: E): Promise<Response>;
}

const ACTOR_DID = "did:web:managon.etzhayyim.com";

export default {
  async fetch(req: Request, env: Env): Promise<Response> {
    const url = new URL(req.url);

    if (url.pathname === "/health") {
      return json({ ok: true, actor: ACTOR_DID });
    }

    if (url.pathname === "/_app/meta") {
      return json({
        ok: true,
        actor: ACTOR_DID,
        nanoid: env.APP_NANOID ?? "m4n4g0n1",
        version: env.APP_VERSION ?? "0.1.0",
        deployedAt: env.APP_DEPLOY_AT ?? null,
        execution: "edge-static",
        displayName: "Minoru Law Office (English)",
        sourceData: "https://www.bengo4.com/mie/a_24204/l_137374/",
      });
    }

    if (url.pathname === "/" || url.pathname === "/index.html") {
      return new Response(renderHome(), {
        status: 200,
        headers: {
          "content-type": "text/html; charset=utf-8",
          "cache-control": "public, max-age=300",
        },
      });
    }

    return new Response("Not Found", {
      status: 404,
      headers: { "content-type": "text/plain; charset=utf-8" },
    });
  },
} satisfies ExportedHandler<Env>;

function json(body: unknown, status = 200): Response {
  return new Response(JSON.stringify(body), {
    status,
    headers: { "content-type": "application/json", "cache-control": "no-store" },
  });
}

function renderHome(): string {
  return `<!doctype html>
<html lang="en">
<head>
<meta charset="utf-8" />
<meta name="viewport" content="width=device-width,initial-scale=1" />
<title>Minoru Law Office — Matsusaka, Mie · attorney Masatoshi Manago</title>
<meta name="description" content="Minoru Law Office (みのる法律事務所) in Matsusaka, Mie — English-language profile of attorney Masatoshi Manago and the firm's practice areas, location, and contact information." />
<style>
  :root {
    --ink: #1a1a1a;
    --ink-muted: #555;
    --paper: #fdfdf9;
    --rule: #d8d4c5;
    --accent: #5a3d2b;
  }
  * { box-sizing: border-box; }
  html, body { margin: 0; background: var(--paper); color: var(--ink);
    font: 16px/1.65 "Iowan Old Style", "Charter", Georgia, "Hiragino Mincho ProN", "Yu Mincho", serif; }
  a { color: var(--accent); }
  .wrap { max-width: 880px; margin: 0 auto; padding: 40px 24px 80px; }
  header.masthead { border-bottom: 2px solid var(--accent); padding-bottom: 18px; margin-bottom: 28px; }
  header.masthead .ja { color: var(--ink-muted); font-size: 0.95em; letter-spacing: 0.05em; }
  header.masthead h1 { margin: 4px 0 6px; font-size: 2.1em; letter-spacing: 0.01em; }
  header.masthead .tagline { color: var(--ink-muted); font-style: italic; font-size: 1.05em; }
  section { margin-bottom: 44px; }
  h2 { font-size: 1.35em; border-bottom: 1px solid var(--rule); padding-bottom: 6px;
    margin: 0 0 16px; letter-spacing: 0.02em; }
  h3 { font-size: 1.05em; margin: 22px 0 6px; color: var(--accent); }
  p { margin: 0 0 12px; }
  ul.areas { list-style: none; padding: 0; display: grid;
    grid-template-columns: repeat(auto-fill, minmax(220px, 1fr)); gap: 8px 16px; }
  ul.areas li { padding: 10px 14px; border: 1px solid var(--rule); border-radius: 4px;
    background: #fff; font-size: 0.97em; }
  ul.areas li b { color: var(--accent); }
  dl.facts { display: grid; grid-template-columns: 160px 1fr; gap: 6px 16px; margin: 0; }
  dl.facts dt { color: var(--ink-muted); font-size: 0.9em; padding-top: 2px; }
  dl.facts dd { margin: 0; }
  table.timeline { border-collapse: collapse; width: 100%; }
  table.timeline th, table.timeline td { text-align: left; vertical-align: top;
    padding: 6px 12px 6px 0; border-bottom: 1px solid var(--rule); font-size: 0.96em; }
  table.timeline th { color: var(--ink-muted); font-weight: normal; width: 130px; white-space: nowrap; }
  blockquote { margin: 0; padding: 14px 18px; border-left: 3px solid var(--accent);
    background: #f6f2e8; font-style: italic; color: var(--ink-muted); }
  footer { border-top: 1px solid var(--rule); padding-top: 18px; margin-top: 56px;
    color: var(--ink-muted); font-size: 0.85em; }
  .meta-grid { display: grid; grid-template-columns: 1fr 1fr; gap: 24px; }
  @media (max-width: 640px) {
    dl.facts { grid-template-columns: 110px 1fr; }
    .meta-grid { grid-template-columns: 1fr; }
    header.masthead h1 { font-size: 1.6em; }
  }
</style>
</head>
<body>
<div class="wrap">

<header class="masthead">
  <div class="ja">みのる法律事務所</div>
  <h1>Minoru Law Office</h1>
  <div class="tagline">Office of Masatoshi Manago, Attorney at Law — Matsusaka, Mie, Japan</div>
</header>

<section id="overview">
  <h2>Overview</h2>
  <p>
    Minoru Law Office is a single-attorney general-practice law firm based in Matsusaka City, Mie Prefecture.
    The principal attorney, <b>Masatoshi Manago</b> (砂子 昌利), is a member of the Mie Bar Association and has been
    in practice since 2010. The firm shares a building with a tax accountant's office and routinely co-ordinates
    with tax accountants and licensed labor and social-security consultants when a matter requires it,
    so clients can access cross-disciplinary support quickly.
  </p>
  <blockquote>
    "I commit myself, every day, to working toward the best possible outcome — so that the clients I serve can smile.
    I look at every matter from multiple angles, and above all I try to think from the client's position."
    — Masatoshi Manago (translation; original Japanese on bengo4.com)
  </blockquote>
  <p>
    The firm welcomes consultations even on matters that other lawyers have declined.
    Where the prospects are difficult, the attorney's stated approach is to say so plainly, while still walking the
    client through whatever paths — even narrow ones — remain available.
  </p>
</section>

<section id="practice-areas">
  <h2>Practice Areas</h2>
  <ul class="areas">
    <li><b>Debt &amp; Bankruptcy</b><br />personal bankruptcy, overpaid-interest claims, illegal money lenders, voluntary settlement, civil rehabilitation</li>
    <li><b>Traffic Accidents</b><br />fatal, property-damage, and personal-injury cases; disability grading, fault allocation, damages</li>
    <li><b>Divorce &amp; Family</b><br />infidelity, separation, DV/abuse, harassment, property division, custody, support, visitation</li>
    <li><b>Inheritance &amp; Estate</b><br />wills, renunciation, heir investigation, partition, statutory-share claims, registration, guardianship</li>
    <li><b>Employment / Labor</b><br />power and sexual harassment, unpaid wages and overtime, unfair dismissal, work-injury recognition</li>
    <li><b>Debt Collection</b><br />commercial and personal claim recovery</li>
    <li><b>Internet &amp; Reputation</b><br />defamation, takedown requests, sender disclosure, damages, criminal complaints</li>
    <li><b>Criminal Defense</b><br />victim and defendant representation, juvenile cases, sex-related offenses, theft, fraud, drug cases</li>
    <li><b>Real Estate &amp; Construction</b><br />transactions, disputes, defects, construction-related litigation</li>
    <li><b>Corporate &amp; General Counsel</b><br />ongoing corporate advisory and outside-counsel engagements</li>
  </ul>
  <p style="margin-top:14px; color:var(--ink-muted); font-size:0.9em;">
    Scope and fees vary by matter. Please confirm directly with the firm.
  </p>
</section>

<section id="attorney">
  <h2>Attorney Profile — Masatoshi Manago (砂子 昌利)</h2>
  <dl class="facts">
    <dt>Bar association</dt>
    <dd>Mie Bar Association (三重弁護士会)</dd>
    <dt>Bar admission</dt>
    <dd>December 2010</dd>
    <dt>Bar examination</dt>
    <dd>Passed September 2009</dd>
    <dt>Committee</dt>
    <dd>Member, Civil-Affairs Anti-Organized-Crime Committee, Daiichi Tokyo Bar Association (April 2015)</dd>
    <dt>Service area</dt>
    <dd>Mie / Kanto region (Tokyo, Kanagawa, Saitama, Chiba, Ibaraki, Tochigi, Gunma)</dd>
  </dl>

  <h3>Education</h3>
  <table class="timeline">
    <tr><th>March 2005</th><td>Tohoku University, Faculty of Law (LL.B.)</td></tr>
    <tr><th>March 2009</th><td>Aoyama Gakuin University Law School (J.D.)</td></tr>
    <tr><th>September 2009</th><td>Passed the Japanese National Bar Examination</td></tr>
    <tr><th>December 2010</th><td>Admitted to the Bar</td></tr>
  </table>

  <h3>Selected Lectures &amp; Seminars</h3>
  <table class="timeline">
    <tr><th>January 2014</th><td>Liaison conference on countermeasures against pseudo-<i>dōwa</i> conduct, Yokohama District Legal Affairs Bureau</td></tr>
    <tr><th>2013</th><td>Lecture: "Police Investigation from a Lawyer's Perspective," Kanagawa Police Academy, Yunodai branch</td></tr>
  </table>

  <h3>Selected Publications</h3>
  <ul>
    <li>"A Study on Petitions for Garnishment Orders under the 'Largest-Deposit Branch Designation' Method" (co-author),
      <i>Specialized Practical Studies 7</i>, Yokohama Bar Association, 2013.</li>
    <li><i>Legal Consultations on Condominiums and Housing Estates</i> (co-author), Gyōsei /
      Yokohama Bar Association (ed.), 2014.</li>
    <li><i>Industry-Specific Manual for Preventing Improper Demands</i> (co-author), Yokohama Bar Association
      Civil-Affairs Anti-Organized-Crime Committee (ed.), 2014.</li>
  </ul>
</section>

<section id="office">
  <h2>Office Information</h2>
  <div class="meta-grid">
    <div>
      <h3>Location</h3>
      <dl class="facts">
        <dt>Postal code</dt><dd>515-0034</dd>
        <dt>Address (JP)</dt><dd>三重県松阪市南町183 2階</dd>
        <dt>Address (EN)</dt><dd>2F, 183 Minamimachi, Matsusaka, Mie 515-0034, Japan</dd>
        <dt>Nearest station</dt><dd>Matsusaka Station (松阪駅) — about 14 minutes on foot</dd>
      </dl>
    </div>
    <div>
      <h3>Hours &amp; Contact</h3>
      <dl class="facts">
        <dt>Phone</dt><dd>+81-598-21-1100 (0598-21-1100)</dd>
        <dt>Open</dt><dd>Weekdays 09:00 – 18:00</dd>
        <dt>Closed</dt><dd>Saturdays, Sundays, public holidays</dd>
        <dt>Note</dt><dd>Evening and weekend consultations are available by prior appointment.</dd>
      </dl>
    </div>
  </div>
</section>

<section id="contact">
  <h2>Contact</h2>
  <p>To reach Minoru Law Office, please use one of the channels below:</p>
  <ul>
    <li><b>Phone:</b> +81-598-21-1100 (Japanese; 09:00 – 18:00 JST, weekdays)</li>
    <li><b>Web inquiry form (Japanese):</b>
      <a href="https://www.bengo4.com/mie/a_24204/l_137374/" rel="noreferrer noopener external">bengo4.com profile page</a></li>
  </ul>
  <p style="font-size:0.9em; color:var(--ink-muted);">
    The firm operates primarily in Japanese. English-speaking visitors should plan to bring a
    Japanese-speaking representative or arrange interpretation in advance.
  </p>
</section>

<footer>
  <p>
    Nothing on this page is legal advice, and reading it does not create an attorney–client relationship.
    For advice on a specific matter, please contact the firm directly.
  </p>
  <p>
    &copy; Minoru Law Office (みのる法律事務所). English-language site at <code>managon.etzhayyim.com</code>.
  </p>
</footer>

</div>
</body>
</html>
`;
}
