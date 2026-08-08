import { useState } from "react";
import DailyReport from "../../components/reports/DailyReport"; // existing shared component
import axiosClient from "../../api/axiosClient";

export default function ReportsReadOnlyPage() {
  const [date, setDate] = useState(new Date().toISOString().slice(0, 10));
  const [report, setReport] = useState(null);

  const load = (d) => axiosClient.get(`/reports/daily?date=${d}`).then(r => setReport(r.data));

  useState(() => { load(date); }, []);

  return (
    <div className="space-y-4">
      <h1 className="text-2xl font-semibold">Daily Report</h1>
      <input
        type="date" value={date}
        onChange={(e) => { setDate(e.target.value); load(e.target.value); }}
        className="border rounded px-3 py-2"
      />
      {report && <DailyReport data={report} readOnly />}
    </div>
  );
}